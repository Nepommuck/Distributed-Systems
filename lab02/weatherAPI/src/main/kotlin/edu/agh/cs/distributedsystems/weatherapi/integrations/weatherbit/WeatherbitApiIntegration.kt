package edu.agh.cs.distributedsystems.weatherapi.integrations.weatherbit

import edu.agh.cs.distributedsystems.weatherapi.integrations.ApiKeys
import edu.agh.cs.distributedsystems.weatherapi.integrations.ApiUrls
import edu.agh.cs.distributedsystems.weatherapi.integrations.WeatherApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.weatherbit.model.WeatherbitResponse
import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Temperature
import edu.agh.cs.distributedsystems.weatherapi.model.TemperatureData
import edu.agh.cs.distributedsystems.weatherapi.model.Weather
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

private object ResponseParser {
    fun parse(response: WeatherbitResponse): Weather = Weather(
        response.data
            .associateBy { it.datetime }
            .mapValues { (_, dailyWeather) ->
                TemperatureData(
                    averageTemperature = Temperature(dailyWeather.temp),
                    minTemperature = Temperature(dailyWeather.min_temp),
                    maxTemperature = Temperature(dailyWeather.max_temp),
                )
            }
    )
}

class WeatherbitApiIntegration : WeatherApiIntegration(ApiUrls.weatherbit) {
    private val key = ApiKeys.weatherbit

    private fun requestDailyWeather(coordinates: Coordinates): Response {
        val url = baseUrl.newBuilder()
            .addQueryParameter("lat", coordinates.lat.toString())
            .addQueryParameter("lon", coordinates.lon.toString())
            .addQueryParameter("key", key)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        return OkHttpClient().newCall(request).execute()
    }

    override fun getWeatherFor(coordinates: Coordinates): Weather? {
        val response = requestDailyWeather(coordinates)
        if (!response.isSuccessful)
            return null

        val weatherbitResponse = gson().fromJson(response.body?.string(), WeatherbitResponse::class.java)
        return ResponseParser.parse(weatherbitResponse)
    }
}
