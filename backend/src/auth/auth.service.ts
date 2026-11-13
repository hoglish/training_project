import { Injectable, Inject, ConflictException, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { Pool } from 'pg';
import * as argon2 from 'argon2';
import * as crypto from 'crypto';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class AuthService {
    constructor(
        @Inject('PG_POOL') private readonly pool: Pool,
        private readonly jwtService: JwtService, private readonly configService: ConfigService,
    ) {}


    private async generateTokens(userId: string, login: string){
        const accessToken = this.jwtService.sign({ sub: userId, login });
        const refreshToken = crypto.randomBytes(40).toString('hex');
        const tokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');

        const expiresInEnv = this.configService.get<string>('JWT_REFRESH_EXPIRES_IN') || '30d';
        const days = parseInt(expiresInEnv.replace('d', ''), 10) || 30;

        const expiresAt = new Date();
        expiresAt.setDate(expiresAt.getDate() + days );

        await this.pool.query(
      `INSERT INTO user_sessions (user_id, token_hash, expires_at) VALUES ($1, $2, $3)`,
      [userId, tokenHash, expiresAt]
    );

    return {
      accessToken,
      refreshToken,
      expiresIn: 900,
      userId: userId,
    };
    }

    async isLoginTaken(login: string): Promise<boolean> {
        const res = await this.pool.query('SELECT id FROM users WHERE login = $1', [login]);
        return res.rows.length > 0;
    }



async register(dto: RegisterDto) {
  if (await this.isLoginTaken(dto.login)) {
    throw new ConflictException('Этот логин уже занят');
  }

  const hashedPassword = await argon2.hash(dto.password);
  const client = await this.pool.connect();

  try {
    await client.query('BEGIN');

    // 1. Создаем пользователя
    const userRes = await client.query(
      'INSERT INTO users (login, password, email) VALUES ($1, $2, $3) RETURNING id, login',
      [dto.login, hashedPassword, dto.email || null]
    );
    const user = userRes.rows[0];

    // 2. Создаем запись в user_facts (добавили date_of_birthday)
    if (dto.gender || dto.height || dto.goal || dto.weight || dto.date_of_birthday) {
      await client.query(
        `INSERT INTO user_facts (user_id, sex, height, goal, weight, role, date_of_birthday) 
         VALUES ($1, $2, $3, $4, $5, 'пользователь', $6)`,
        [
          user.id,
          dto.gender || null,
          dto.height || null,
          dto.goal || null,
          dto.weight || null,
          dto.date_of_birthday || null, // <-- ДОБАВИЛИ ДАТУ
        ]
      );
    }

    // 3. Создаем первую запись в истории весов
    if (dto.weight) {
      await client.query(
        `INSERT INTO weights (user_id, weight, date) VALUES ($1, $2, CURRENT_DATE)`,
        [user.id, dto.weight]
      );
    }

    await client.query('COMMIT');
    return this.generateTokens(user.id, user.login);

  } catch (error: any) {
    await client.query('ROLLBACK');
    if (error.code === '23505') throw new ConflictException('Логин занят');
    throw error;
  } finally {
    client.release();
  }
}

    async login(dto: LoginDto){
        const res = await this.pool.query('SELECT id, login, password FROM users WHERE login = $1', [dto.login]);
        if (res.rows.length === 0){
             throw new UnauthorizedException('Неверный логин или пароль');
        }

        const user = res.rows[0];

        const isValid = await argon2.verify(user.password, dto.password);
        if (!isValid){
            throw new UnauthorizedException('Неверный логин или пароль');
        }
            return this.generateTokens(user.id, user.login);

    }

  async refresh(refreshToken: string) {
    const tokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');

    const res = await this.pool.query(
      `SELECT user_id, id FROM user_sessions 
       WHERE token_hash = $1 AND is_revoked = false AND expires_at > NOW()`,
      [tokenHash]
    );

    if (res.rows.length === 0) {
      throw new UnauthorizedException('Сессия не найдена или истекла. Войдите заново.');
    }

    const session = res.rows[0];

    await this.pool.query(
      `UPDATE user_sessions SET is_revoked = true WHERE id = $1`,
      [session.id]
    );

    const userRes = await this.pool.query('SELECT login FROM users WHERE id = $1', [session.user_id]);
    const userLogin = userRes.rows[0].login;

    return this.generateTokens(session.user_id, userLogin);
  }


}
