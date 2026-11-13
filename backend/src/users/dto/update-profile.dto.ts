import { IsOptional, IsString, IsInt, Min, Max, IsNumber, IsDateString, IsIn} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateProfileDto {
    @ApiProperty({example: "test@email.ru", required: false})
    @IsOptional()
    @IsString()
    email?: string;

    @ApiProperty({ example: '1995-05-20', required: false })
  @IsOptional()
  @IsDateString()
  date_of_birthday?: string;

@ApiProperty({ example: 'м', required: false })
@IsOptional()
@IsString()
@IsIn(['м', 'ж'])
gender?: string;

  @ApiProperty({ example: 180, required: false })
  @IsOptional()
  @IsInt()
  @Min(100)
  @Max(250)
  height?: number;

  @ApiProperty({ example: 75.5, required: false })
  @IsOptional()
  @IsNumber()
  @Min(20)
  @Max(300)
  weight?: number;

  @ApiProperty({ example: 'похудеть', required: false })
  @IsOptional()
  @IsString()
  goal?: string;
}