package data.remote.api

import kotlinx.coroutines.Deferred
import data.remote.models.CurrentWeather
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("current.json")
    fun getCurrentWeatherAsync(
        @Query("key") apiKey: String,
        @Query("q") queryCountry: String,
        @Query("aqi") aqi: String // air quality data
    ): Deferred<CurrentWeather>
}