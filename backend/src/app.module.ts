import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { DatabaseModule } from './database/database.module';
import { NutritionModule } from './nutrition/nutrition.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }), // Оставляем чтение .env
    DatabaseModule, NutritionModule,                           // Оставляем подключение к БД
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}