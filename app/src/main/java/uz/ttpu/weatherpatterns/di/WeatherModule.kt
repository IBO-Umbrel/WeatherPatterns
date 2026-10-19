package uz.ttpu.weatherpatterns.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uz.ttpu.weatherpatterns.data.api.FakeWeatherApi
import uz.ttpu.weatherpatterns.data.repository.WeatherRepository
import uz.ttpu.weatherpatterns.mvvm.viewmodel.WeatherViewModel
import uz.ttpu.weatherpatterns.mvi.viewmodel.WeatherMviViewModel

object WeatherModule
{
    // Three seconds leaves time to rotate the device while loading.
    val repository = WeatherRepository(FakeWeatherApi(latencyMs = 3_000))

    val weatherViewModelFactory: ViewModelProvider.Factory = viewModelFactory()
    {
        initializer()
        {
            WeatherViewModel(repository)
        }
    }

    val weatherMviViewModelFactory: ViewModelProvider.Factory = viewModelFactory()
    {
        initializer()
        {
            WeatherMviViewModel(repository)
        }
    }
}
