import { ApiProperty } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsOptional, IsString, IsNumberString } from 'class-validator';

export class GetTagsFilterDto {
  @ApiProperty({ 
    required: false, 
    description: 'Поиск по названию', 
    example: 'мяс' 
  })
  @Transform(({ value }) => value?.toLowerCase())
  @IsOptional()
  @IsString()
  search?: string;

  @ApiProperty({ 
    required: false, 
    description: 'Количество записей (по умолчанию 20)', 
    example: 10 
  })
  @IsOptional()
  @IsNumberString()
  limit?: string;

  @ApiProperty({ 
    required: false, 
    description: 'Смещение для пагинации', 
    example: 0 
  })
  @IsOptional()
  @IsNumberString()
  offset?: string;
}