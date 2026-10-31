import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { DatabaseModule } from './database/database.module';
import { NutritionModule } from './nutrition/nutrition.module';
import { TagsModule } from './shared/tags/tags.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }), // Оставляем чтение .env
    DatabaseModule, NutritionModule, TagsModule,                           // Оставляем подключение к БД
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}