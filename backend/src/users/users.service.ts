import { Injectable, Inject } from '@nestjs/common';
import { Pool } from 'pg';

@Injectable()
export class UsersService {
  constructor(@Inject('PG_POOL') private readonly pool: Pool) {}

  async findAll() {
    const result = await this.pool.query('SELECT id, login, email FROM users');
    return result.rows;
  }
}