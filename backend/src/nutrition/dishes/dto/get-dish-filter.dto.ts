import { ApiProperty } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsOptional, IsString, IsNumberString, isString } from 'class-validator';

export class GetDishesFilterDto{
    @ApiProperty({ required: false, description: 'Поиск по названию', example: 'салат' })
    @Transform(({value}) => value?.toLowerCase())
    @IsOptional()
    @IsString()
    search?: string;

    @ApiProperty({ required: false, description: 'Минимальная калорийность', example: 100 })
    @IsOptional()
    @IsNumberString()
    min_calorie?: string;

    @ApiProperty({ required: false, description: 'Максимальная калорийность', example: 500 })
    @IsOptional()
    @IsNumberString()
    max_calorie?: string;

    @ApiProperty({ required: false, description: 'Теги через запятую (например, мясо,диетическое)' })
    @IsOptional()
    @IsString()
    tags?: string;

    @ApiProperty({ required: false, description: 'Количество записей (по умолчанию 10)', example: 10 })
    @IsOptional()
    @IsNumberString()
    limit?: string;

    @ApiProperty({ required: false, description: 'Смещение', example: 0 })
    @IsOptional()
    @IsNumberString()
    offset?: string;


}