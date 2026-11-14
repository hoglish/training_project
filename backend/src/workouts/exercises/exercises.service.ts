import {
  Injectable,
  Inject,
  ConflictException,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { Pool } from 'pg';
import { CreateExerciseDto } from './dto/create-exercise.dto';
import { UpdateExerciseDto } from './dto/update-exercise.dto';
import { GetExercisesFilterDto } from './dto/get-exersices-filter.dto';

@Injectable()
export class ExercisesService {
  constructor(@Inject('PG_POOL') private readonly pool: Pool) {}

  async create(dto: CreateExerciseDto) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const res = await client.query(
        `INSERT INTO workouts (name, workout_type, animation)
         VALUES ($1, $2, $3) RETURNING *`,
        [dto.name, dto.workout_type, dto.animation ?? null],
      );
      const workout = res.rows[0];

      if (dto.muscules?.length) {
        await this.insertRelations(client, 'workout_muscules', 'muscul_id', workout.id, dto.muscules);
      }
      if (dto.inventories?.length) {
        await this.insertRelations(client, 'workout_inventories', 'inventory_id', workout.id, dto.inventories);
      }
      if (dto.tags?.length) {
        await this.insertRelations(client, 'workout_tags', 'tag_id', workout.id, dto.tags);
      }

      await client.query('COMMIT');
      return this.findOne(workout.id);
    } catch (error: any) {
      await client.query('ROLLBACK');
      if (error.code === '23505') {
        throw new ConflictException(`Упражнение "${dto.name}" уже существует`);
      }
      if (error.code === '23503') {
        throw new BadRequestException('Несуществующая мышца / инвентарь / тег');
      }
      throw error;
    } finally {
      client.release();
    }
  }

  async findAll(filters: GetExercisesFilterDto) {
    const limit = filters.limit ?? 5;
    const offset = filters.offset ?? 0;

    const { whereClause, values } = this.buildWhere(filters);

    const dataQuery = `
      SELECT * FROM workouts
      ${whereClause}
      ORDER BY LOWER(name) ASC
      LIMIT $${values.length + 1} OFFSET $${values.length + 2}
    `;
    const dataRes = await this.pool.query(dataQuery, [...values, limit, offset]);

    const countQuery = `SELECT COUNT(*) FROM workouts ${whereClause}`;
    const countRes = await this.pool.query(countQuery, values);
    const total = parseInt(countRes.rows[0].count, 10);

    return {
      data: dataRes.rows,
      meta: {
        limit,
        offset,
        total,
        totalPages: Math.ceil(total / limit),
        currentPage: Math.floor(offset / limit) + 1,
      },
    };
  }

  async findOne(id: string) {
    const res = await this.pool.query('SELECT * FROM workouts WHERE id = $1', [id]);
    if (res.rows.length === 0) {
      throw new NotFoundException(`Упражнение с ID ${id} не найдено`);
    }
    const workout = res.rows[0];

    const [muscules, inventories, tags] = await Promise.all([
      this.pool.query(
        `SELECT m.* FROM muscules m
         JOIN workout_muscules wm ON wm.muscul_id = m.id
         WHERE wm.workout_id = $1`,
        [id],
      ),
      this.pool.query(
        `SELECT i.* FROM inventories i
         JOIN workout_inventories wi ON wi.inventory_id = i.id
         WHERE wi.workout_id = $1`,
        [id],
      ),
      this.pool.query(
        `SELECT t.* FROM tags t
         JOIN workout_tags wt ON wt.tag_id = t.id
         WHERE wt.workout_id = $1`,
        [id],
      ),
    ]);

    return {
      ...workout,
      muscules: muscules.rows,
      inventories: inventories.rows,
      tags: tags.rows,
    };
  }

  async update(id: string, dto: UpdateExerciseDto) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const exists = await client.query('SELECT id FROM workouts WHERE id = $1', [id]);
      if (exists.rows.length === 0) {
        throw new NotFoundException(`Упражнение с ID ${id} не найдено`);
      }

      const fields: string[] = [];
      const values: any[] = [];
      let i = 1;

      if (dto.name !== undefined) { fields.push(`name = $${i++}`); values.push(dto.name); }
      if (dto.workout_type !== undefined) { fields.push(`workout_type = $${i++}`); values.push(dto.workout_type); }
      if (dto.animation !== undefined) { fields.push(`animation = $${i++}`); values.push(dto.animation); }

      if (fields.length > 0) {
        values.push(id);
        await client.query(`UPDATE workouts SET ${fields.join(', ')} WHERE id = $${i}`, values);
      }

      if (dto.muscules !== undefined) {
        await client.query('DELETE FROM workout_muscules WHERE workout_id = $1', [id]);
        if (dto.muscules.length) {
          await this.insertRelations(client, 'workout_muscules', 'muscul_id', id, dto.muscules);
        }
      }
      if (dto.inventories !== undefined) {
        await client.query('DELETE FROM workout_inventories WHERE workout_id = $1', [id]);
        if (dto.inventories.length) {
          await this.insertRelations(client, 'workout_inventories', 'inventory_id', id, dto.inventories);
        }
      }
      if (dto.tags !== undefined) {
        await client.query('DELETE FROM workout_tags WHERE workout_id = $1', [id]);
        if (dto.tags.length) {
          await this.insertRelations(client, 'workout_tags', 'tag_id', id, dto.tags);
        }
      }

      await client.query('COMMIT');
      return this.findOne(id);
    } catch (error: any) {
      await client.query('ROLLBACK');
      if (error.code === '23505') {
        throw new ConflictException('Упражнение с таким названием уже существует');
      }
      if (error.code === '23503') {
        throw new BadRequestException('Несуществующая мышца / инвентарь / тег');
      }
      throw error;
    } finally {
      client.release();
    }
  }

  async remove(id: string) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const exists = await client.query('SELECT id FROM workouts WHERE id = $1', [id]);
      if (exists.rows.length === 0) {
        throw new NotFoundException(`Упражнение с ID ${id} не найдено`);
      }

      await client.query('DELETE FROM workout_muscules WHERE workout_id = $1', [id]);
      await client.query('DELETE FROM workout_inventories WHERE workout_id = $1', [id]);
      await client.query('DELETE FROM workout_tags WHERE workout_id = $1', [id]);

      const res = await client.query('DELETE FROM workouts WHERE id = $1 RETURNING *', [id]);

      await client.query('COMMIT');
      return { message: 'Упражнение удалено', workout: res.rows[0] };
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  private async insertRelations(
    client: any,
    table: string,
    refColumn: string,
    workoutId: string,
    ids: string[],
  ) {
    await client.query(
      `INSERT INTO ${table} (workout_id, ${refColumn})
       SELECT $1, unnest($2::uuid[])
       ON CONFLICT DO NOTHING`,
      [workoutId, ids],
    );
  }

  private buildWhere(filters: GetExercisesFilterDto): { whereClause: string; values: any[] } {
    const conditions: string[] = [];
    const values: any[] = [];
    let i = 1;

    if (filters.search) {
      conditions.push(`name LIKE $${i}`);
      values.push(`%${filters.search}%`);
      i++;
    }
    if (filters.workout_type) {
      conditions.push(`workout_type = $${i}`);
      values.push(filters.workout_type);
      i++;
    }
    if (filters.muscules) {
      const ids = this.parseUuidList(filters.muscules);
      if (ids.length) {
        conditions.push(`id IN (
          SELECT workout_id FROM workout_muscules
          WHERE muscul_id = ANY($${i}::uuid[])
        )`);
        values.push(ids);
        i++;
      }
    }
    if (filters.inventories) {
      const ids = this.parseUuidList(filters.inventories);
      if (ids.length) {
        conditions.push(`id IN (
          SELECT workout_id FROM workout_inventories
          WHERE inventory_id = ANY($${i}::uuid[])
        )`);
        values.push(ids);
        i++;
      }
    }
    if (filters.tags) {
      const ids = this.parseUuidList(filters.tags);
      if (ids.length) {
        conditions.push(`id IN (
          SELECT workout_id FROM workout_tags
          WHERE tag_id = ANY($${i}::uuid[])
        )`);
        values.push(ids);
        i++;
      }
    }

    const whereClause = conditions.length ? `WHERE ${conditions.join(' AND ')}` : '';
    return { whereClause, values };
  }

  private parseUuidList(raw: string): string[] {
    return raw.split(',').map((s) => s.trim()).filter((s) => s.length > 0);
  }
}