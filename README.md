Поиск блюд основывается на имени и на фильтре по количеству каллорий и по тегам
 @ApiProperty({ required: false, description: 'Теги через запятую (например, мясо,диетическое)' })
    @IsOptional()
    @IsString()
    tags?: string;
