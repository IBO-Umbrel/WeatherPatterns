package uz.ttpu.weatherpatterns

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.ttpu.weatherpatterns.data.api.FakeWeatherApi
import uz.ttpu.weatherpatterns.data.model.Weather
import uz.ttpu.weatherpatterns.data.repository.WeatherRepository
import uz.ttpu.weatherpatterns.mvp.WeatherContract
import uz.ttpu.weatherpatterns.mvp.presenter.WeatherPresenter

class FakeView : WeatherContract.View
{
    val events = mutableListOf<String>()

    override fun showLoading()
    {
        events += "loading"
    }

    override fun showWeather(weather: Weather)
    {
        events += "weather:${weather.city}"
    }

    override fun showError(message: String)
    {
        events += "error:$message"
    }
}

class WeatherPresenterTest
{
    private val view = FakeView()
    private val presenter = WeatherPresenter(
        view = view,
        repository = WeatherRepository(FakeWeatherApi(latencyMs = 0)),
        scope = CoroutineScope(Dispatchers.Unconfined),
    )

    @Test
    fun known_city_shows_loading_then_weather()
    {
        presenter.onCitySelected("Tashkent")
        assertEquals(listOf("loading", "weather:Tashkent"), view.events)
    }

    @Test
    fun unknown_city_shows_loading_then_error()
    {
        presenter.onCitySelected("Atlantis")
        assertEquals(listOf("loading", "error:No forecast for Atlantis"), view.events)
    }

    @Test
    fun two_requests_in_a_row_show_two_loading_events()
    {
        presenter.onCitySelected("Tashkent")
        presenter.onCitySelected("Samarkand")
        assertEquals(
            listOf("loading", "weather:Tashkent", "loading", "weather:Samarkand"),
            view.events,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun destroying_presenter_cancels_pending_callback() = runTest()
    {
        val delayedPresenter = WeatherPresenter(
            view = view,
            repository = WeatherRepository(FakeWeatherApi(latencyMs = 3_000)),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        delayedPresenter.onCitySelected("Bukhara")
        delayedPresenter.destroy()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("loading"), view.events)
    }
}
