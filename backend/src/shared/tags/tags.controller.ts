import { Controller, Get, Post, Body, Query, Param } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { TagsService } from './tags.service';
import { CreateTagDto } from './dto/create-tag.dto';
import { GetTagsFilterDto } from './dto/get-tag-filter.dto';

@ApiTags('Теги (Общие)')
@Controller('tags')
export class TagsController {
  constructor(private readonly tagsService: TagsService) {}

  @Get()
  @ApiOperation({ summary: 'Получить список тегов с поиском и пагинацией' })
  findAll(@Query() filters: GetTagsFilterDto) {
    return this.tagsService.findAll(filters);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Получить тег по ID' })
  @ApiResponse({ status: 200, description: 'Тег найден' })
  @ApiResponse({ status: 404, description: 'Тег не найден' })
  findOne(@Param('id') id: string) {
    return this.tagsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Создать новый тег' })
  @ApiResponse({ status: 201, description: 'Тег создан' })
  @ApiResponse({ status: 409, description: 'Тег уже существует' })
  create(@Body() dto: CreateTagDto) {
    return this.tagsService.create(dto);
  }
}