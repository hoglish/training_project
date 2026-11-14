import { Module } from '@nestjs/common';
import { ExercisesController } from './exercises/exercises.controller';
import { ExercisesService } from './exercises/exercises.service';
import { TrainingsService } from './trainings/trainings.service';
import { TrainingsController } from './trainings/trainings.controller';
import { ExercisesModule } from './exercises/exercises.module';
import { TrainingsModule } from './trainings/trainings.module';

@Module({
  controllers: [ExercisesController, TrainingsController],
  providers: [ExercisesService, TrainingsService],
  imports: [ExercisesModule, TrainingsModule]
})
export class WorkoutsModule {}
