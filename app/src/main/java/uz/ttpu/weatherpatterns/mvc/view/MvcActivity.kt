package uz.ttpu.weatherpatterns.mvc.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import uz.ttpu.weatherpatterns.di.WeatherModule
import uz.ttpu.weatherpatterns.mvc.controller.WeatherController
import uz.ttpu.weatherpatterns.mvc.model.WeatherModel
import uz.ttpu.weatherpatterns.ui.WeatherScreen

class MvcActivity : ComponentActivity(), WeatherView
{
    private val model = WeatherModel(WeatherModule.repository)
    private val controller = WeatherController(model, view = this)
    private var status by mutableStateOf("Pick a city")

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContent()
        {
            MaterialTheme()
            {
                WeatherScreen(
                    status = status,
                    onCitySelected = controller::onCitySelected
                )
            }
        }
    }

    // The View reads the Model directly, so render needs no parameters.
    // Passing Weather would make the Controller supply the display data instead.
    override fun render()
    {
        status = when
        {
            model.isLoading -> "Loading…"
            model.error != null -> "Error: ${model.error}"
            model.weather != null ->
            {
                val w = model.weather!!
                "${w.city}: ${w.temperatureC}°C, ${w.condition}"
            }

            else -> "Pick a city"
        }
    }
}
