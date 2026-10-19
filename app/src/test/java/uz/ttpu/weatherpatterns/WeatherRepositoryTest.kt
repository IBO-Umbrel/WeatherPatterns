package uz.ttpu.weatherpatterns

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import uz.ttpu.weatherpatterns.data.api.FakeWeatherApi
import uz.ttpu.weatherpatterns.data.api.WeatherApi
import uz.ttpu.weatherpatterns.data.model.Weather
import uz.ttpu.weatherpatterns.data.repository.WeatherRepository

class WeatherRepositoryTest
{
    private val repository = WeatherRepository(FakeWeatherApi(latencyMs = 0))

    @Test
    fun known_city_returns_success() = runBlocking<Unit>()
    {
        val result = repository.getWeather("Tashkent")
        assertTrue(result.isSuccess)
        assertEquals(24, result.getOrThrow().temperatureC)
    }

    @Test
    fun unknown_city_returns_failure() = runBlocking<Unit>()
    {
        val result = repository.getWeather("Atlantis")
        assertTrue(result.isFailure)
        assertEquals("No forecast for Atlantis", result.exceptionOrNull()?.message)
    }

    @Test(expected = CancellationException::class)
    fun cancellation_is_not_returned_as_failure(): Unit = runBlocking()
    {
        val api = object : WeatherApi
        {
            override suspend fun fetchWeather(city: String): Weather
            {
                throw CancellationException("Cancelled")
            }
        }
        WeatherRepository(api).getWeather("Tashkent")
        Unit
    }
}
