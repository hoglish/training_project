import { IsString, MinLength, MaxLength, IsOptional, IsEmail, IsInt, Min, IsIn, IsDateString } from 'class-validator';
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

@ApiProperty({ example: 'похудеть на 5 кг', required: false })
@IsOptional()
@IsString()
goal?: string;

@ApiProperty({ example: 'м', required: false })
@IsOptional()
@IsString()
@IsIn(['м', 'ж'])
gender?: string;

@ApiProperty({ example: 180, required: false })
@IsOptional()
@IsInt()
@Min(40)
height?: number;

  @ApiProperty({ example: 75.5, required: false })
  @IsOptional()
  weight?: number;

  @ApiProperty({ example: '1995-05-20', description: 'Дата рождения в формате YYYY-MM-DD', required: false })
  @IsOptional()
  @IsDateString() 
  date_of_birthday?: string;

}