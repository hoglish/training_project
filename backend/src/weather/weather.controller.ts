import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { WeatherService } from './weather.service';

@ApiTags('Погода')
@Controller('weather')
export class WeatherController {
  constructor(private readonly weatherService: WeatherService) {}

  @Get()
  @ApiOperation({ summary: 'Погода: текущая + почасовая + дневная (7 дней)' })
  getWeather(@Query('city') city?: string) {
    return this.weatherService.getWeather(city || 'Москва');
  }
}