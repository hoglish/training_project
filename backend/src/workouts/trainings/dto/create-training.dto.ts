import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import {
  IsString, IsOptional, IsNumber, IsArray, IsUUID, IsISO8601,
  ValidateNested, Min, Max, MaxLength,
} from 'class-validator';

export class ExerciseSetDto {
  @ApiProperty({ example: 'uuid_упражнения' })
  @IsUUID('4')
  workout_id: string;

  @ApiProperty({ example: 1 })
  @IsNumber()
  @Min(1)
  set_number: number;

  @ApiProperty({ example: 10 })
  @IsNumber()
  @Min(1)
  number_of_repeat: number;

  @ApiProperty({ example: 50 })
  @IsNumber()
  @Min(0)
  weight: number;
}

export class CreateTrainingDto {
  @ApiProperty({ example: 'вечерняя тренировка' })
  @IsString()
  @MaxLength(40)
  name: string;

  @ApiProperty({ example: '2026-11-14T18:00:00Z' })
  @IsISO8601()
  date_time_start: string;

  @ApiProperty({ example: '2026-11-14T19:30:00Z' })
  @IsISO8601()
  date_time_end: string;

  @ApiProperty({ example: 'запланирована', required: false })
  @IsOptional()
  @IsString()
  @MaxLength(40)
  status?: string;

  @ApiProperty({ example: 4, required: false })
  @IsOptional()
  @IsNumber()
  @Min(1)
  @Max(5)
  feeling?: number;

  @ApiProperty({ example: 'хорошая тренировка', required: false })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiProperty({ example: 0, required: false })
  @IsOptional()
  @IsNumber()
  @Min(0)
  calories?: number;

  @ApiProperty({ type: [ExerciseSetDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ExerciseSetDto)
  sets: ExerciseSetDto[];
}