package uz.ttpu.weatherpatterns.data.repository

import uz.ttpu.weatherpatterns.data.api.WeatherApi
import uz.ttpu.weatherpatterns.data.model.Weather

class WeatherRepository(private val api: WeatherApi)
{
    suspend fun getWeather(city: String): Result<Weather>
    {
        try
        {
            val weather = api.fetchWeather(city)
            return Result.success(weather)
        }
        catch (e: IllegalArgumentException)
        {
            return Result.failure(e)
        }
    }
}
