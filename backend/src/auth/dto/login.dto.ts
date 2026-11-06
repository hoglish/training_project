import { IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class LoginDto {
    @ApiProperty({ example: 'testuser', description: 'Логин пользователя' })
  @IsString()
  login: string;

  @ApiProperty({ example: '123456', description: 'Пароль' })
  @IsString()
  password: string;
}