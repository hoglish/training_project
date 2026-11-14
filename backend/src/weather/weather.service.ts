import { Injectable, Inject, Logger } from '@nestjs/common';
import { Pool } from 'pg';
import axios from 'axios';

@Injectable()
export class WeatherService {
  private readonly logger = new Logger(WeatherService.name);

  private readonly YANDEX_KEY = process.env.YANDEX_WEATHER_KEY || '';
  private readonly YANDEX_URL = 'https://api.weather.yandex.ru/v2/forecast';

  private readonly DEFAULT_LAT = 55.75;
  private readonly DEFAULT_LON = 37.62;

  private dailyCache = new Map<string, { data: any[]; ts: number }>();
  private readonly DAILY_TTL_MS = 60 * 60 * 1000;

  constructor(@Inject('PG_POOL') private readonly pool: Pool) {
    if (!this.YANDEX_KEY) {
      this.logger.warn('YANDEX_WEATHER_KEY не задан — погода не будет работать');
    }
  }

  async getWeather(city: string = 'Москва') {
    const today = new Date().toISOString().slice(0, 10);

    const cachedCurrent = await this.pool.query(
      `SELECT id,
              TO_CHAR(date, 'YYYY-MM-DD') AS date,
              temperature, city_name, weather_condition,
              precipitation_probability, temperature_night
         FROM weather_caches
        WHERE city_name = $1 AND date = $2`,
      [city, today],
    );

    let hourlyRows: any[] = [];
    if (cachedCurrent.rows.length > 0) {
      const r = await this.pool.query(
        `SELECT TO_CHAR(time, 'HH24:MI') AS time,
                temperature, weather_condition, precipitation_probability
           FROM weather_cache_per_days
          WHERE weather_cache_id = $1
          ORDER BY time ASC`,
        [cachedCurrent.rows[0].id],
      );
      hourlyRows = r.rows;
    }

    if (cachedCurrent.rows.length === 0 || hourlyRows.length === 0) {
      try {
        await this.fetchAndStoreFromYandex(city, today);
      } catch (e: any) {
        this.logger.error(`Yandex fetch failed: ${e.message}`);
      }

      const reCurrent = await this.pool.query(
        `SELECT id,
                TO_CHAR(date, 'YYYY-MM-DD') AS date,
                temperature, city_name, weather_condition,
                precipitation_probability, temperature_night
           FROM weather_caches
          WHERE city_name = $1 AND date = $2`,
        [city, today],
      );

      if (reCurrent.rows.length > 0) {
        const r = await this.pool.query(
          `SELECT TO_CHAR(time, 'HH24:MI') AS time,
                  temperature, weather_condition, precipitation_probability
             FROM weather_cache_per_days
            WHERE weather_cache_id = $1
            ORDER BY time ASC`,
          [reCurrent.rows[0].id],
        );
        return {
          current: reCurrent.rows[0],
          hourly: r.rows,
          daily: await this.getDaily(city),
        };
      }
    }

    if (cachedCurrent.rows.length > 0) {
      return {
        current: cachedCurrent.rows[0],
        hourly: hourlyRows,
        daily: await this.getDaily(city),
      };
    }

    return {
      current: {
        city_name: city,
        date: today,
        temperature: 0,
        weather_condition: 'нет данных',
        precipitation_probability: 0,
        temperature_night: 0,
      },
      hourly: [],
      daily: [],
    };
  }

  private async fetchAndStoreFromYandex(city: string, today: string) {
    const resp = await axios.get(this.YANDEX_URL, {
      headers: { 'X-Yandex-Weather-Key': this.YANDEX_KEY },
      params: {
        lat: this.DEFAULT_LAT,
        lon: this.DEFAULT_LON,
        lang: 'ru_RU',
        limit: 2,
      },
      timeout: 15000,
    });

    const data = resp.data;
    if (!data.fact || !data.forecasts) {
      throw new Error(`Некорректный ответ от Яндекс.Погоды для "${city}"`);
    }

    const fact = data.fact;
    const todayForecast = data.forecasts[0];

    const currentRow = {
      city_name: city,
      date: today,
      temperature: fact.temp,
      weather_condition: this.decodeYandexCondition(fact.condition),
      precipitation_probability: 0,
      temperature_night: todayForecast.parts?.night?.temp_avg ?? fact.temp,
    };

    const insertedCurrent = await this.pool.query(
      `INSERT INTO weather_caches
         (city_name, date, temperature, weather_condition, precipitation_probability, temperature_night)
       VALUES ($1, $2, $3, $4, $5, $6)
       ON CONFLICT (city_name, date) DO UPDATE SET
         temperature = EXCLUDED.temperature,
         weather_condition = EXCLUDED.weather_condition,
         precipitation_probability = EXCLUDED.precipitation_probability,
         temperature_night = EXCLUDED.temperature_night
       RETURNING id`,
      [
        currentRow.city_name,
        currentRow.date,
        currentRow.temperature,
        currentRow.weather_condition,
        currentRow.precipitation_probability,
        currentRow.temperature_night,
      ],
    );

    const cacheId = insertedCurrent.rows[0].id;
    this.logger.log(`weather_caches id for ${city} = ${cacheId}`);

    const hours = todayForecast.hours || [];
    this.logger.log(`hourly points received: ${hours.length}`);

    for (const h of hours) {
      const timeStr = `${String(h.hour).padStart(2, '0')}:00`;
      try {
        await this.pool.query(
          `INSERT INTO weather_cache_per_days
             (weather_cache_id, time, temperature, city_name, weather_condition, precipitation_probability, temperature_night)
           VALUES ($1, $2, $3, $4, $5, $6, $7)
           ON CONFLICT (weather_cache_id, time) DO UPDATE SET
             temperature = EXCLUDED.temperature,
             city_name = EXCLUDED.city_name,
             weather_condition = EXCLUDED.weather_condition,
             precipitation_probability = EXCLUDED.precipitation_probability,
             temperature_night = EXCLUDED.temperature_night`,
          [
            cacheId,
            timeStr,
            h.temp,
            city,
            this.decodeYandexCondition(h.condition),
            Math.round((h.prec_strength ?? 0) * 100),
            h.temp,
          ],
        );
      } catch (e: any) {
        this.logger.error(`insert hourly ${timeStr} failed: ${e.message}`);
      }
    }

    this.dailyCache.set(city, {
      data: this.buildDaily(data.forecasts),
      ts: Date.now(),
    });
  }

  private async getDaily(city: string): Promise<any[]> {
    const cached = this.dailyCache.get(city);
    if (cached && Date.now() - cached.ts < this.DAILY_TTL_MS) {
      return cached.data;
    }
    return cached?.data ?? [];
  }

  private buildDaily(forecasts: any[]): any[] {
    return forecasts.map((day: any) => {
      const parts = day.parts || {};
      const dayPart = parts.day || parts.morning || {};
      return {
        date: day.date,
        temp_max: dayPart.temp_max ?? dayPart.temp_avg ?? 0,
        temp_min: parts.night?.temp_min ?? parts.night?.temp_avg ?? 0,
        condition: this.decodeYandexCondition(dayPart.condition || 'clear'),
        precipitation_probability: Math.round((dayPart.prec_strength ?? 0) * 100),
      };
    });
  }

  private decodeYandexCondition(condition: string): string {
    const map: Record<string, string> = {
      'clear': 'ясно',
      'partly-cloudy': 'малооблачно',
      'cloudy': 'облачно с прояснениями',
      'overcast': 'пасмурно',
      'light-rain': 'небольшой дождь',
      'rain': 'дождь',
      'heavy-rain': 'сильный дождь',
      'showers': 'ливень',
      'wet-snow': 'дождь со снегом',
      'light-snow': 'небольшой снег',
      'snow': 'снег',
      'snow-showers': 'снегопад',
      'hail': 'град',
      'thunderstorm': 'гроза',
      'thunderstorm-with-rain': 'дождь с грозой',
      'thunderstorm-with-hail': 'гроза с градом',
    };
    return map[condition] || 'неизвестно';
  }
}