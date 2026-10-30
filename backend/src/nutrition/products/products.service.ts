import { Injectable, Inject } from '@nestjs/common';
import { Pool } from 'pg';
import { CreateProductDto } from './dto/create-product.dto';
import { GetProductsFilterDto } from './dto/get-products-filter.dto';

@Injectable()
export class ProductsService {
    constructor(@Inject('PG_POOL') private readonly pool: Pool) {}

    async create(dto: CreateProductDto) {
        const query = `
            INSERT INTO products (name, protein, oil, carb, calorie)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING *;
        `;
        
        const values = [dto.name, dto.protein, dto.oil, dto.carb, dto.calorie];
        
        const res = await this.pool.query(query, values);
        return res.rows[0];
    }

    async findAll(filters: GetProductsFilterDto) {
        const limit = filters.limit ? parseInt(filters.limit) : 10;
        const offset = filters.offset ? parseInt(filters.offset) : 0;

        let query = 'SELECT * FROM products WHERE 1=1';
        const values: any[] = [];
        let paramIndex = 1;

        if (filters.search) {
            query += ` AND name LIKE $${paramIndex}`;
            values.push(`%${filters.search}%`);
            paramIndex++;
        }

        query += ` ORDER BY name ASC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
        values.push(limit, offset);

        const res = await this.pool.query(query, values);

        return {
            data: res.rows,
            meta: { limit, offset }
        };
    }

    async findOne(id: string){
        const res = await this.pool.query('SELECT * FROM products WHERE id = $1', [id]);
        return res.rows[0];
    }
}