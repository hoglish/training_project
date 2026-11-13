import { IsOptional, IsBoolean, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateNotificationsDto {
  @ApiProperty({ example: true, required: false })
  @IsOptional()
  @IsBoolean()
  remind_training?: boolean;

  @ApiProperty({ example: true, required: false })
  @IsOptional()
  @IsBoolean()
  remind_meals?: boolean;

  @ApiProperty({ example: true, required: false })
  @IsOptional()
  @IsBoolean()
  remind_water?: boolean;

  @ApiProperty({ example: '23:00', required: false })
  @IsOptional()
  @IsString()
  quiet_mode_start?: string;

  @ApiProperty({ example: '07:00', required: false })
  @IsOptional()
  @IsString()
  quiet_mode_end?: string;
}