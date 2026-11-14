import {
  Controller, Get, Post, Patch, Delete,
  Body, Param, Query, UseGuards, Req,
} from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags, ApiBearerAuth, ApiOperation,
  ApiOkResponse, ApiCreatedResponse, ApiResponse,
} from '@nestjs/swagger';
import { TrainingsService } from './trainings.service';
import { CreateTrainingDto } from './dto/create-training.dto';
import { UpdateTrainingDto } from './dto/update-training.dto';
import { GetTrainingsFilterDto } from './dto/get-trainings-filter.dto';

@ApiTags('Тренировки')
@ApiBearerAuth()
@Controller('workouts/trainings')
@UseGuards(AuthGuard('jwt'))
export class TrainingsController {
  constructor(private readonly trainingsService: TrainingsService) {}

  @Post()
  @ApiOperation({ summary: 'Создать тренировку с подходами' })
  @ApiCreatedResponse({ description: 'Создана' })
  create(@Req() req: any, @Body() dto: CreateTrainingDto) {
    return this.trainingsService.create(req.user.userId, dto);
  }

  @Get()
  @ApiOperation({ summary: 'Список тренировок пользователя' })
  @ApiOkResponse({ description: 'Постраничный список' })
  findAll(@Req() req: any, @Query() filters: GetTrainingsFilterDto) {
    return this.trainingsService.findAll(req.user.userId, filters);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Тренировка с подходами' })
  @ApiResponse({ status: 404, description: 'Не найдена' })
  findOne(@Req() req: any, @Param('id') id: string) {
    return this.trainingsService.findOne(id, req.user.userId);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Обновить тренировку' })
  update(@Req() req: any, @Param('id') id: string, @Body() dto: UpdateTrainingDto) {
    return this.trainingsService.update(id, req.user.userId, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Удалить тренировку' })
  remove(@Req() req: any, @Param('id') id: string) {
    return this.trainingsService.remove(id, req.user.userId);
  }
}