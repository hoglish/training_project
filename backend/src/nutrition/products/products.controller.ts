import { Controller, Get, Post, Body, Query, Param } from '@nestjs/common';
import { ProductsService } from './products.service';
import { CreateProductDto } from './dto/create-product.dto';
import { GetProductsFilterDto } from './dto/get-products-filter.dto';
import { ApiTags, ApiOperation, ApiResponse, ApiOkResponse  } from '@nestjs/swagger'; 

@ApiTags('Продукты')
@Controller('nutrition/products')
export class ProductsController {
    constructor(private readonly productService: ProductsService){};

    @Post()
    @ApiOperation({ summary: 'Создать новый продукт', description: 'Принимает название и КБЖУ, возвращает созданный объект с UUID' })
    @ApiResponse({ status: 201, description: 'Продукт успешно создан' })
    @ApiResponse({ status: 400, description: 'Неверные данные (например, отрицательные калории)' })
    create(@Body() dto: CreateProductDto){
        return this.productService.create(dto);
    }

    @Get()
    @ApiOperation({ summary: 'Получить список продуктов' })
    @ApiOkResponse({
    description: 'Список продуктов',
    schema: {
      example: {
        data: [
          {
            id: '26dc29e8-f0b9-415e-98a5-40f48ba8c088',
            name: 'куриная грудка',
            protein: '23.5',
            oil: '2.0',
            carb: '0.0',
            calorie: '113.0'
          }
        ],
        meta: { limit: 10, offset: 0 }
      }
    }
  })
    findAll(@Query() filters: GetProductsFilterDto){
        return this.productService.findAll(filters);
    }

    @Get(':id')
    @ApiOperation({ summary: 'Получить список продуктов', description: 'Возвращает список с поиском по имени и пагинацией' })
    findOne(@Param("id") id: string){
        return this.productService.findOne(id);
    }
}
