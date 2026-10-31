import { Controller, Get, Post, Body, Query, Param } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { DishesService } from './dishes.service';
import { CreateDishDto } from './dto/create-dish.dto';
import { GetDishesFilterDto } from './dto/get-dish-filter.dto';

@ApiTags('Блюда (Nutrition)')
@Controller('nutrition/dishes')
export class DishesController {
  constructor(private readonly dishesService: DishesService) {}

  @Post()
  @ApiOperation({ summary: 'Создать новое блюдо с продуктами и тегами' })
  @ApiResponse({ status: 201, description: 'Блюдо создано' })
  @ApiResponse({ status: 400, description: 'Ошибка в данных (неверный продукт, единица измерения и т.д.)' })
  create(@Body() dto: CreateDishDto) {
    return this.dishesService.create(dto);
  }

  @Get()
  @ApiOperation({ summary: 'Получить список блюд (всегда отсортированы по возрастанию калорий)' })
  findAll(@Query() filters: GetDishesFilterDto) {
    return this.dishesService.findAll(filters);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Получить блюдо по ID' })
  @ApiResponse({ status: 200, description: 'Блюдо найдено' })
  @ApiResponse({ status: 404, description: 'Блюдо не найдено' })
  findOne(@Param('id') id: string) {
    return this.dishesService.findOne(id);
  }
}