import { ApiProperty } from "@nestjs/swagger";
import { Transform } from "class-transformer";
import { IsString, MaxLength } from "class-validator";

export class CreateTagDto {
    @ApiProperty ({example: 'мясо', description: 'Название тега'})
    @Transform(({value})=> value?.toLowerCase().trim())
    @IsString()
    @MaxLength(40)
    name: string;
}