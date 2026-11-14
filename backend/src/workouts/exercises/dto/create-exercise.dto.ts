import { ApiProperty } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import {
  IsString,
  IsOptional,
  IsArray,
  IsUUID,
  MaxLength,
} from 'class-validator';

export class CreateExerciseDto {
  @ApiProperty({ example: 'жим лежа' })
  @Transform(({ value }) => value?.toLowerCase().trim())
  @IsString()
  @MaxLength(50)
  name: string;

  @ApiProperty({ example: 'базовое' })
  @Transform(({ value }) => value?.toLowerCase().trim())
  @IsString()
  @MaxLength(50)
  workout_type: string;

  @ApiProperty({ example: 'https://example.com/anim.gif', required: false })
  @IsOptional()
  @IsString()
  @MaxLength(255)
  animation?: string;

  @ApiProperty({ example: ['uuid1'], required: false })
  @IsOptional()
  @IsArray()
  @IsUUID('4', { each: true })
  muscules?: string[];

  @ApiProperty({ example: ['uuid1'], required: false })
  @IsOptional()
  @IsArray()
  @IsUUID('4', { each: true })
  inventories?: string[];

  @ApiProperty({ example: ['uuid1'], required: false })
  @IsOptional()
  @IsArray()
  @IsUUID('4', { each: true })
  tags?: string[];
}