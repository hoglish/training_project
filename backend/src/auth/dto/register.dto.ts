import { IsString, MinLength, MaxLength, IsOptional, IsEmail } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';


export class RegisterDto {
    @ApiProperty({ example: 'testuser', description: 'Логин пользователя' })
  @IsString()
  @MinLength(3)
  @MaxLength(30)
  login: string;

  @ApiProperty({ example: '123456', description: 'Пароль (минимум 6 символов)' })
  @IsString()
  @MinLength(6)
  @MaxLength(255)
  password: string;


    @ApiProperty({ example: 'test@mail.ru', description: 'Email (необязательно)', required: false })
  @IsOptional()
  @IsEmail()
  email?: string;
}