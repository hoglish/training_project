import {
  Injectable, Inject, NotFoundException, BadRequestException,
} from '@nestjs/common';
import { Pool } from 'pg';
import { CreateTrainingDto } from './dto/create-training.dto';
import { UpdateTrainingDto } from './dto/update-training.dto';
import { GetTrainingsFilterDto } from './dto/get-trainings-filter.dto';

@Injectable()
export class TrainingsService {
  constructor(@Inject('PG_POOL') private readonly pool: Pool) {}

  async create(userId: string, dto: CreateTrainingDto) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      if (dto.sets?.length) {
        const uniqueIds = [...new Set(dto.sets.map((s) => s.workout_id))];
        const check = await client.query(
          'SELECT id FROM workouts WHERE id = ANY($1::uuid[])',
          [uniqueIds],
        );
        if (check.rows.length !== uniqueIds.length) {
          throw new BadRequestException('Одно или несколько упражнений не существуют');
        }
      }

      const res = await client.query(
        `INSERT INTO trainings
           (name, date_time_start, date_time_end, status, feeling, description, calories, user_id)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
         RETURNING *`,
        [
          dto.name,
          dto.date_time_start,
          dto.date_time_end,
          dto.status ?? 'запланирована',
          dto.feeling ?? null,
          dto.description ?? null,
          dto.calories ?? 0,
          userId,
        ],
      );
      const training = res.rows[0];

      if (dto.sets?.length) {
        const uniqueIds = [...new Set(dto.sets.map((s) => s.workout_id))];
        await client.query(
          `INSERT INTO training_workouts (training_id, workout_id)
           SELECT $1, unnest($2::uuid[])
           ON CONFLICT DO NOTHING`,
          [training.id, uniqueIds],
        );

        const workoutIds = dto.sets.map((s) => s.workout_id);
        const setNumbers = dto.sets.map((s) => s.set_number);
        const reps = dto.sets.map((s) => s.number_of_repeat);
        const weights = dto.sets.map((s) => s.weight);

        await client.query(
          `INSERT INTO exercise_sets
             (training_id, workout_id, set_number, number_of_repeat, weight)
           SELECT $1, unnest($2::uuid[]), unnest($3::int[]), unnest($4::int[]), unnest($5::smallint[])`,
          [training.id, workoutIds, setNumbers, reps, weights],
        );
      }

      await client.query('COMMIT');
      return this.findOne(training.id, userId);
    } catch (error: any) {
      await client.query('ROLLBACK');
      if (error.code === '23503') {
        throw new BadRequestException('Указано несуществующее упражнение');
      }
      throw error;
    } finally {
      client.release();
    }
  }

  async findAll(userId: string, filters: GetTrainingsFilterDto) {
    const limit = filters.limit ?? 5;
    const offset = filters.offset ?? 0;

    const conditions: string[] = ['user_id = $1'];
    const values: any[] = [userId];
    let i = 2;

    if (filters.date_from) {
      conditions.push(`date_time_start >= $${i}`);
      values.push(filters.date_from);
      i++;
    }
    if (filters.date_to) {
      conditions.push(`date_time_start < ($${i}::date + INTERVAL '1 day')`);
      values.push(filters.date_to);
      i++;
    }
    if (filters.search) {
      conditions.push(`name LIKE $${i}`);
      values.push(`%${filters.search.toLowerCase()}%`);
      i++;
    }

    const whereClause = `WHERE ${conditions.join(' AND ')}`;

    const dataQuery = `
      SELECT * FROM trainings
      ${whereClause}
      ORDER BY date_time_start DESC
      LIMIT $${i} OFFSET $${i + 1}
    `;
    const dataRes = await this.pool.query(dataQuery, [...values, limit, offset]);

    const countRes = await this.pool.query(
      `SELECT COUNT(*) FROM trainings ${whereClause}`,
      values,
    );
    const total = parseInt(countRes.rows[0].count, 10);

    return {
      data: dataRes.rows,
      meta: {
        limit, offset, total,
        totalPages: Math.ceil(total / limit),
        currentPage: Math.floor(offset / limit) + 1,
      },
    };
  }

  async findOne(id: string, userId: string) {
    const res = await this.pool.query(
      'SELECT * FROM trainings WHERE id = $1 AND user_id = $2',
      [id, userId],
    );
    if (res.rows.length === 0) {
      throw new NotFoundException(`Тренировка с ID ${id} не найдена`);
    }
    const training = res.rows[0];

    const setsRes = await this.pool.query(
      `SELECT es.*, w.name AS workout_name, w.workout_type
       FROM exercise_sets es
       JOIN workouts w ON w.id = es.workout_id
       WHERE es.training_id = $1
       ORDER BY w.name ASC, es.set_number ASC`,
      [id],
    );

    const grouped: Record<string, any> = {};
    for (const row of setsRes.rows) {
      if (!grouped[row.workout_id]) {
        grouped[row.workout_id] = {
          workout_id: row.workout_id,
          workout_name: row.workout_name,
          workout_type: row.workout_type,
          sets: [],
        };
      }
      grouped[row.workout_id].sets.push({
        id: row.id,
        set_number: row.set_number,
        number_of_repeat: row.number_of_repeat,
        weight: row.weight,
      });
    }

    return { ...training, exercises: Object.values(grouped) };
  }

  async update(id: string, userId: string, dto: UpdateTrainingDto) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const exists = await client.query(
        'SELECT id FROM trainings WHERE id = $1 AND user_id = $2',
        [id, userId],
      );
      if (exists.rows.length === 0) {
        throw new NotFoundException(`Тренировка с ID ${id} не найдена`);
      }

      const fields: string[] = [];
      const values: any[] = [];
      let i = 1;

      if (dto.name !== undefined) { fields.push(`name = $${i++}`); values.push(dto.name); }
      if (dto.date_time_start !== undefined) { fields.push(`date_time_start = $${i++}`); values.push(dto.date_time_start); }
      if (dto.date_time_end !== undefined) { fields.push(`date_time_end = $${i++}`); values.push(dto.date_time_end); }
      if (dto.status !== undefined) { fields.push(`status = $${i++}`); values.push(dto.status); }
      if (dto.feeling !== undefined) { fields.push(`feeling = $${i++}`); values.push(dto.feeling); }
      if (dto.description !== undefined) { fields.push(`description = $${i++}`); values.push(dto.description); }
      if (dto.calories !== undefined) { fields.push(`calories = $${i++}`); values.push(dto.calories); }

      if (fields.length > 0) {
        values.push(id);
        await client.query(`UPDATE trainings SET ${fields.join(', ')} WHERE id = $${i}`, values);
      }

      if (dto.sets !== undefined) {
        if (dto.sets.length) {
          const uniqueIds = [...new Set(dto.sets.map((s) => s.workout_id))];
          const check = await client.query(
            'SELECT id FROM workouts WHERE id = ANY($1::uuid[])',
            [uniqueIds],
          );
          if (check.rows.length !== uniqueIds.length) {
            throw new BadRequestException('Одно или несколько упражнений не существуют');
          }
        }

        await client.query('DELETE FROM exercise_sets WHERE training_id = $1', [id]);
        await client.query('DELETE FROM training_workouts WHERE training_id = $1', [id]);

        if (dto.sets.length) {
          const uniqueIds = [...new Set(dto.sets.map((s) => s.workout_id))];
          await client.query(
            `INSERT INTO training_workouts (training_id, workout_id)
             SELECT $1, unnest($2::uuid[])`,
            [id, uniqueIds],
          );

          const workoutIds = dto.sets.map((s) => s.workout_id);
          const setNumbers = dto.sets.map((s) => s.set_number);
          const reps = dto.sets.map((s) => s.number_of_repeat);
          const weights = dto.sets.map((s) => s.weight);

          await client.query(
            `INSERT INTO exercise_sets
               (training_id, workout_id, set_number, number_of_repeat, weight)
             SELECT $1, unnest($2::uuid[]), unnest($3::int[]), unnest($4::int[]), unnest($5::smallint[])`,
            [id, workoutIds, setNumbers, reps, weights],
          );
        }
      }

      await client.query('COMMIT');
      return this.findOne(id, userId);
    } catch (error: any) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  async remove(id: string, userId: string) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const exists = await client.query(
        'SELECT id FROM trainings WHERE id = $1 AND user_id = $2',
        [id, userId],
      );
      if (exists.rows.length === 0) {
        throw new NotFoundException(`Тренировка с ID ${id} не найдена`);
      }

      await client.query('DELETE FROM exercise_sets WHERE training_id = $1', [id]);
      await client.query('DELETE FROM training_workouts WHERE training_id = $1', [id]);
      const res = await client.query('DELETE FROM trainings WHERE id = $1 RETURNING *', [id]);

      await client.query('COMMIT');
      return { message: 'Тренировка удалена', training: res.rows[0] };
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }
}