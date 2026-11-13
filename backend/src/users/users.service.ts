import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { Pool } from 'pg';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { UpdateNotificationsDto } from './dto/update-notifications.dto';

@Injectable()
export class UsersService {
    constructor(@Inject('PG_POOL') private readonly pool: Pool){}

    async getProfile(userId: string){
        const query = `
      SELECT 
        u.login, 
        u.email, 
        uf.sex AS gender, 
        uf.height, 
        uf.weight, 
        uf.goal, 
        uf.date_of_birthday
      FROM users u
      LEFT JOIN user_facts uf ON u.id = uf.user_id
      WHERE u.id = $1
    `;
    const res = await this.pool.query(query,[userId]);

    if(res.rows.length === 0){
        throw new NotFoundException('Пользователь не найден')
    }
    return res.rows[0];
    }

    async updateProfile(userId: string, dto: UpdateProfileDto){
        const client = await this.pool.connect();

        try{
            await client.query('BEGIN');

            if(dto.email){
                await client.query(
                    'UPDATE users SET email = $1 WHERE id = $2', [dto.email, userId]
                )
            }

        const updateFactsQuery = `
        UPDATE user_facts 
        SET 
          sex = COALESCE($1, sex),
          height = COALESCE($2, height),
          weight = COALESCE($3, weight),
          goal = COALESCE($4, goal),
          date_of_birthday = COALESCE($5, date_of_birthday)
        WHERE user_id = $6
      `;
      
      await client.query(updateFactsQuery, [
        dto.gender || null,
        dto.height || null,
        dto.weight || null,
        dto.goal || null,
        dto.date_of_birthday || null,
        userId
      ]);

      await client.query('COMMIT'); 
      return { success: true, message: 'Профиль обновлен' };
        } catch (error) {
      await client.query('ROLLBACK'); 
      throw error;
    } finally {
      client.release();
    }
    }

      async updateNotifications(userId: string, dto: UpdateNotificationsDto) {
    const query = `
      INSERT INTO notification_settings (
        user_id, remind_training, remind_meals, remind_water, quiet_mode_start, quiet_mode_end
      ) VALUES ($1, $2, $3, $4, $5, $6)
      ON CONFLICT (user_id) DO UPDATE SET
        remind_training = COALESCE(EXCLUDED.remind_training, notification_settings.remind_training),
        remind_meals = COALESCE(EXCLUDED.remind_meals, notification_settings.remind_meals),
        remind_water = COALESCE(EXCLUDED.remind_water, notification_settings.remind_water),
        quiet_mode_start = COALESCE(EXCLUDED.quiet_mode_start, notification_settings.quiet_mode_start),
        quiet_mode_end = COALESCE(EXCLUDED.quiet_mode_end, notification_settings.quiet_mode_end)
    `;
    // Если пришло null, ставим дефолты
    const values = [
      userId,
      dto.remind_training ?? true,
      dto.remind_meals ?? true,
      dto.remind_water ?? true,
      dto.quiet_mode_start || null,
      dto.quiet_mode_end || null
    ];
    await this.pool.query(query, values);
    return { success: true };
  }
    async getNotifications(userId: string) {
    const res = await this.pool.query(
      'SELECT remind_training, remind_meals, remind_water, quiet_mode_start, quiet_mode_end FROM notification_settings WHERE user_id = $1',
      [userId]
    );
    if (res.rows.length === 0) {
      // Если настроек нет, отдаем дефолтные
      return {
        remind_training: true,
        remind_meals: true,
        remind_water: true,
        quiet_mode_start: null,
        quiet_mode_end: null,
      };
    }
    return res.rows[0];
  }
}
