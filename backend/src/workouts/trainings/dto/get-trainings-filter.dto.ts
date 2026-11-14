import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsOptional, IsString, IsNumber, IsISO8601, Min, Max } from 'class-validator';

export class GetTrainingsFilterDto {
  @ApiProperty({ required: false, example: '2026-11-01' })
  @IsOptional()
  @IsISO8601()
  date_from?: string;

  @ApiProperty({ required: false, example: '2026-11-30' })
  @IsOptional()
  @IsISO8601()
  date_to?: string;

  @ApiProperty({ required: false, example: 'вечерняя' })
  @IsOptional()
  @IsString()
  search?: string;

  @ApiProperty({ required: false, example: 5 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  @Max(50)
  limit?: number;

  @ApiProperty({ required: false, example: 0 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  offset?: number;
}