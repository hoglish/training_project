import { ApiProperty } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsString, IsNumber, Min } from 'class-validator';

export class CreateProductDto {
  @ApiProperty({ 
    example: 'куриная грудка', 
    description: 'Название продукта' 
  })
  @Transform(({ value }) => value?.toLowerCase().trim())
  @IsString()
  name: string;

  @ApiProperty({ 
    example: 23.5, 
    description: 'Белки на 100г' 
  })
  @IsNumber()
  @Min(0)
  protein: number;

  @ApiProperty({ 
    example: 2.0, 
    description: 'Жиры на 100г' 
  })
  @IsNumber()
  @Min(0)
  oil: number;

  @ApiProperty({ 
    example: 0, 
    description: 'Углеводы на 100г' 
  })
  @IsNumber()
  @Min(0)
  carb: number;

  @ApiProperty({ 
    example: 113, 
    description: 'Калорийность на 100г' 
  })
  @IsNumber()
  @Min(0)
  calorie: number;
}