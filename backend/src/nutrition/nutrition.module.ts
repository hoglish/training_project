import { Module } from '@nestjs/common';
import { ProductsController } from './products/products.controller';
import { ProductsService } from './products/products.service';
import { DishesController } from './dishes/dishes.controller';
import { DishesService } from './dishes/dishes.service';

@Module({
  controllers: [ProductsController, DishesController],
  providers: [ProductsService, DishesService]
})
export class NutritionModule {}
