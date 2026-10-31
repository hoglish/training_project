import { Injectable, Inject, BadRequestException, NotFoundException } from '@nestjs/common';
import { Pool } from 'pg';
import { CreateDishDto } from './dto/create-dish.dto';
import { GetDishesFilterDto } from './dto/get-dish-filter.dto';

@Injectable()
export class DishesService {
  constructor(@Inject('PG_POOL') private readonly pool: Pool) {}

  async create(dto: CreateDishDto) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const dishRes = await client.query(
        'INSERT INTO dishes (name, description) VALUES ($1, $2) RETURNING *',
        [dto.name, dto.description || null]
      );
      const dishId = dishRes.rows[0].id;

      for (const product of dto.products) {
        await client.query(
          'INSERT INTO products_in_dish (dish_id, product_id, quantity, unit_of_measure) VALUES ($1, $2, $3, $4)',
          [dishId, product.product_id, product.quantity, product.unit_of_measure]
        );
      }

      for (const tagName of dto.tags) {
        let tagRes = await client.query('SELECT id FROM tags WHERE name = $1', [tagName]);
        let tagId;
        if (tagRes.rows.length === 0) {
          const newTag = await client.query('INSERT INTO tags (name) VALUES ($1) RETURNING id', [tagName]);
          tagId = newTag.rows[0].id;
        } else {
          tagId = tagRes.rows[0].id;
        }
        await client.query('INSERT INTO dishes_tags (dish_id, tag_id) VALUES ($1, $2)', [dishId, tagId]);
      }

      await client.query('COMMIT');
      return { ...dishRes.rows[0], products: dto.products, tags: dto.tags };
    } catch (error: any) {
      await client.query('ROLLBACK');
      if (error.code === '23503') throw new BadRequestException('Указан несуществующий продукт или тег');
      if (error.code === '23505') throw new BadRequestException('Этот продукт уже добавлен в блюдо');
      if (error.code === '23514') throw new BadRequestException('Недопустимая единица измерения');
      throw new BadRequestException('Ошибка при создании блюда');
    } finally {
      client.release();
    }
  }

  async findAll(filters: GetDishesFilterDto) {
    const limit = filters.limit ? parseInt(filters.limit) : 10;
    const offset = filters.offset ? parseInt(filters.offset) : 0;
    
    const values: any[] = [];
    let paramIndex = 1;

    let query = `
      WITH dish_calories AS (
        SELECT 
          d.id, 
          d.name, 
          d.description,
          COALESCE(SUM(
            (p.calorie / 100.0) * pid.quantity * 
            CASE 
              WHEN pid.unit_of_measure IN ('г', 'гр', 'грамм', 'грамма', 'граммов') THEN 1
              WHEN pid.unit_of_measure IN ('кг', 'килограмм', 'килограмма', 'килограммов') THEN 1000
              WHEN pid.unit_of_measure IN ('мл', 'миллилитр', 'миллилитра', 'миллилитров') THEN 1
              WHEN pid.unit_of_measure IN ('л', 'литр', 'литра', 'литров') THEN 1000
              WHEN pid.unit_of_measure IN ('шт', 'штука', 'штуки', 'штук') THEN 100
              WHEN pid.unit_of_measure IN ('порт', 'порция', 'порции', 'порций') THEN 250
              WHEN pid.unit_of_measure IN ('ч. л.', 'ч.л.', 'чайная ложка', 'чайные ложки', 'чайных ложек') THEN 5
              WHEN pid.unit_of_measure IN ('ст. л.', 'ст.л.', 'столовая ложка', 'столовые ложки', 'столовых ложек') THEN 15
              WHEN pid.unit_of_measure IN ('стакан', 'стакана', 'стаканов') THEN 200
              WHEN pid.unit_of_measure IN ('щепотка', 'щепотки', 'щепоток') THEN 1
              ELSE 1 
            END
          ), 0) AS total_calorie
        FROM dishes d
        LEFT JOIN products_in_dish pid ON d.id = pid.dish_id
        LEFT JOIN products p ON pid.product_id = p.id
        GROUP BY d.id
      )
      SELECT 
        dc.*,
        COALESCE(
          json_agg(DISTINCT jsonb_build_object('id', t.id, 'name', t.name)) 
          FILTER (WHERE t.id IS NOT NULL), '[]'
        ) as tags
      FROM dish_calories dc
      LEFT JOIN dishes_tags dt ON dc.id = dt.dish_id
      LEFT JOIN tags t ON dt.tag_id = t.id
      WHERE 1=1
    `;

    if (filters.search) {
      query += ` AND dc.name LIKE $${paramIndex}`;
      values.push(`%${filters.search}%`);
      paramIndex++;
    }

    if (filters.min_calorie) {
      query += ` AND dc.total_calorie >= $${paramIndex}`;
      values.push(parseFloat(filters.min_calorie));
      paramIndex++;
    }

    if (filters.max_calorie) {
      query += ` AND dc.total_calorie <= $${paramIndex}`;
      values.push(parseFloat(filters.max_calorie));
      paramIndex++;
    }

    if (filters.tags) {
      const tagNames = filters.tags.split(',').map(t => t.trim().toLowerCase());
      query += `
        AND dc.id IN (
          SELECT dt2.dish_id 
          FROM dishes_tags dt2
          JOIN tags t2 ON dt2.tag_id = t2.id
          WHERE t2.name = ANY($${paramIndex})
          GROUP BY dt2.dish_id
          HAVING COUNT(DISTINCT t2.name) = $${paramIndex + 1}
        )
      `;
      values.push(tagNames, tagNames.length);
      paramIndex += 2;
    }

    query += ` GROUP BY dc.id, dc.name, dc.description, dc.total_calorie`;
    query += ` ORDER BY dc.total_calorie ASC`; // Всегда легкие сверху
    query += ` LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
    
    values.push(limit, offset);

    const res = await this.pool.query(query, values);

    return {
      data: res.rows,
      meta: { limit, offset }
    };
  }

  async findOne(id: string) {
    const res = await this.pool.query(
      `SELECT 
        d.*,
        COALESCE(
          json_agg(DISTINCT jsonb_build_object('id', t.id, 'name', t.name)) 
          FILTER (WHERE t.id IS NOT NULL), '[]'
        ) as tags
      FROM dishes d
      LEFT JOIN dishes_tags dt ON d.id = dt.dish_id
      LEFT JOIN tags t ON dt.tag_id = t.id
      WHERE d.id = $1
      GROUP BY d.id`,
      [id]
    );

    if (res.rows.length === 0) {
      throw new NotFoundException(`Блюдо с ID ${id} не найдено`);
    }
    return res.rows[0];
  }
}