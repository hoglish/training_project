import { Controller, Post, Body, Get, Query, UseGuards, Req } from '@nestjs/common';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { UnauthorizedException } from '@nestjs/common';

@ApiTags('Auth')
@Controller('auth')
export class AuthController {
    constructor(private readonly authService: AuthService){}

    @Get('check-login')
  @ApiOperation({ summary: 'Проверить, занят ли логин' })
  async checkLogin(@Query('login') login: string) {
    return { isTaken: await this.authService.isLoginTaken(login) };
  }

   @Post('register')
  @ApiOperation({ summary: 'Регистрация' })
  async register(@Body() dto: RegisterDto) {
    return this.authService.register(dto);
  }

  @Post('login')
  @ApiOperation({ summary: 'Вход' })
  async login(@Body() dto: LoginDto) {
    return this.authService.login(dto);
  }

    @Post('refresh')
  @ApiOperation({ summary: 'Обновить Access Token' })
  async refresh(@Body('refreshToken') refreshToken: string) {
    if (!refreshToken) {
      throw new UnauthorizedException('Токен не передан');
    }
    return this.authService.refresh(refreshToken);
  }


}
