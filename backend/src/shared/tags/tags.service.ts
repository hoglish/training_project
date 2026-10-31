import { Injectable, Inject, ConflictException, NotFoundException } from '@nestjs/common';
import { Pool } from 'pg';
import { CreateTagDto } from './dto/create-tag.dto';
import { GetTagsFilterDto } from './dto/get-tag-filter.dto';


@Injectable()
export class TagsService {
    constructor(@Inject('PG_POOL') private readonly pool: Pool) {}
    
 async findAll(filters: GetTagsFilterDto) {
        const limit = filters.limit ? parseInt(filters.limit) : 20;
        const offset = filters.offset ? parseInt(filters.offset) : 0;

        let query = 'SELECT * FROM tags WHERE 1=1';
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

    async findOne(id: string) {
        const res = await this.pool.query('SELECT * FROM tags WHERE id = $1', [id]);
        
        if (res.rows.length === 0) {
            throw new NotFoundException(`Тег с ID ${id} не найден`);
        }
        
        return res.rows[0];
    }

    async create(dto: CreateTagDto){
        const query = 'INSERT INTO tags (name) VALUES ($1) RETURNING *';
        try{
            const res = await this.pool.query(query, [dto.name]);
            return res.rows[0];
        } catch (error: any){
            if(error.code === '23505'){
                throw new ConflictException(`Тег "${dto.name}" уже существует`);
            }
            throw error;
        }
    }
}
