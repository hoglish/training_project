import { ApiProperty } from '@nestjs/swagger';
import { Transform, Type } from 'class-transformer';
import { IsOptional, IsString, IsNumber, Min, Max } from 'class-validator';

export class GetExercisesFilterDto {
  @ApiProperty({ required: false, example: 'жим' })
  @IsOptional()
  @IsString()
  @Transform(({ value }) => value?.toLowerCase().trim())
  search?: string;

  @ApiProperty({ required: false, example: 'базовое' })
  @IsOptional()
  @IsString()
  @Transform(({ value }) => value?.toLowerCase().trim())
  workout_type?: string;

  @ApiProperty({ required: false, description: 'ID мышц через запятую' })
  @IsOptional()
  @IsString()
  muscules?: string;

  @ApiProperty({ required: false, description: 'ID инвентаря через запятую' })
  @IsOptional()
  @IsString()
  inventories?: string;

  @ApiProperty({ required: false, description: 'ID тегов через запятую' })
  @IsOptional()
  @IsString()
  tags?: string;

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