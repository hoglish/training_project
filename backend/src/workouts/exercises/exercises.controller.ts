import {
  Controller, Get, Post, Patch, Delete,
  Body, Param, Query, UseGuards,
} from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags, ApiBearerAuth, ApiOperation,
  ApiOkResponse, ApiCreatedResponse, ApiResponse,
} from '@nestjs/swagger';
import { ExercisesService } from './exercises.service';
import { CreateExerciseDto } from './dto/create-exercise.dto';
import { UpdateExerciseDto } from './dto/update-exercise.dto';
import { GetExercisesFilterDto } from './dto/get-exersices-filter.dto';

@ApiTags('Упражнения')
@ApiBearerAuth()
@Controller('workouts/exercises')
@UseGuards(AuthGuard('jwt'))
export class ExercisesController {
  constructor(private readonly exercisesService: ExercisesService) {}

  @Post()
  @ApiOperation({ summary: 'Создать упражнение' })
  @ApiCreatedResponse({ description: 'Создано' })
  @ApiResponse({ status: 409, description: 'Уже существует' })
  create(@Body() dto: CreateExerciseDto) {
    return this.exercisesService.create(dto);
  }

  @Get()
  @ApiOperation({ summary: 'Список упражнений с фильтрами' })
  @ApiOkResponse({ description: 'Постраничный список' })
  findAll(@Query() filters: GetExercisesFilterDto) {
    return this.exercisesService.findAll(filters);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Получить упражнение по ID' })
  @ApiResponse({ status: 404, description: 'Не найдено' })
  findOne(@Param('id') id: string) {
    return this.exercisesService.findOne(id);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Обновить упражнение' })
  update(@Param('id') id: string, @Body() dto: UpdateExerciseDto) {
    return this.exercisesService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Удалить упражнение' })
  remove(@Param('id') id: string) {
    return this.exercisesService.remove(id);
  }
}