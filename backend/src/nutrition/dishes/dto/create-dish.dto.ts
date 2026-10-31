import { ApiProperty } from "@nestjs/swagger";
import { Transform, Type } from "class-transformer";

import { IsString, IsArray, ValidateNested, IsNumber, IsUUID, IsOptional, Min, MaxLength, Max, isArray } from "class-validator";

export class DishProductDto{
    @ApiProperty({ example: '26dc29e8-f0b9-415e-98a5-40f48ba8c088', description: 'UUID продукта' })
    @IsUUID()
    product_id: string;

    @ApiProperty({ example: 100, description: 'Количество (больше 0)' })
    @IsNumber()
    @Min(1)
    quantity: number;

    @ApiProperty({ 
    example: 'г', 
    description: 'Единица измерения (г, кг, мл, шт, порция и т.д.)' 
  })
    @IsString()
    unit_of_measure: string;
}

export class CreateDishDto{
    @ApiProperty({ example: 'салат цезарь', description: 'Название блюда' })
    @Transform(({value}) => value?.toLowerCase().trim())
    @IsString()
    @MaxLength(50)
    name: string;

    @ApiProperty({ example: 'вкусный салат', description: 'Описание блюда', required: false })
    @IsOptional()
    @IsString()
    description?: string;

    @ApiProperty({ type: [DishProductDto], description: 'Список продуктов в блюде' })
    @IsArray()
    @ValidateNested({each: true})
    @Type(()=> DishProductDto)
    products: DishProductDto[];

    @ApiProperty({ example: ['мясо', 'диетическое'], description: 'Список тегов' })
    @IsArray()
    @IsString({each: true})
    tags: string[];

}