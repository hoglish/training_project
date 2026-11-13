import { Controller, Get, Patch, Body, Req, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { UpdateNotificationsDto } from './dto/update-notifications.dto';

@ApiTags('Users (Профиль)')
@ApiBearerAuth() 
@Controller('users')
@UseGuards(AuthGuard('jwt')) 
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get('me')
  @ApiOperation({ summary: 'Получить профиль текущего пользователя' })
  async getMe(@Req() req: any) {
    return this.usersService.getProfile(req.user.userId);
  }

  @Patch('me')
  @ApiOperation({ summary: 'Обновить профиль текущего пользователя' })
  async updateMe(@Req() req: any, @Body() dto: UpdateProfileDto) {
    return this.usersService.updateProfile(req.user.userId, dto);
  }

    @Patch('me/notifications')
  @ApiOperation({ summary: 'Обновить настройки уведомлений' })
  async updateNotifications(@Req() req: any, @Body() dto: UpdateNotificationsDto) {
    return this.usersService.updateNotifications(req.user.userId, dto);
  }

    @Get('me/notifications')
  @ApiOperation({ summary: 'Получить настройки уведомлений' })
  async getNotifications(@Req() req: any) {
    return this.usersService.getNotifications(req.user.userId);
  }

}