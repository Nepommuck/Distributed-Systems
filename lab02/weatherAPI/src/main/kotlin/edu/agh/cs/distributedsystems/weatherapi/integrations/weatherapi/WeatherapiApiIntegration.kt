package edu.agh.cs.distributedsystems.weatherapi.integrations.weatherapi

import edu.agh.cs.distributedsystems.weatherapi.integrations.ApiKeys
import edu.agh.cs.distributedsystems.weatherapi.integrations.ApiUrls
import edu.agh.cs.distributedsystems.weatherapi.integrations.WeatherApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.weatherapi.model.WeatherapiResponse
import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Temperature
import edu.agh.cs.distributedsystems.weatherapi.model.TemperatureData
import edu.agh.cs.distributedsystems.weatherapi.model.Weather
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

private object ResponseParser {
    fun parse(response: WeatherapiResponse): Weather = Weather(
        response.forecast.forecastday
            .associateBy { it.date }
            .mapValues { (_, dailyWeather) ->
                val data = dailyWeather.day
                TemperatureData(
                    averageTemperature = Temperature(data.avgtemp_c),
                    minTemperature = Temperature(data.mintemp_c),
                    maxTemperature = Temperature(data.maxtemp_c),
                )
            }
    )
}

class WeatherapiApiIntegration : WeatherApiIntegration(ApiUrls.weatherapi) {
    private val key = ApiKeys.weatherapi

    private fun requestDailyWeather(coordinates: Coordinates): Response {
        val url = baseUrl.newBuilder()
            .addQueryParameter("q", "${coordinates.lat},${coordinates.lon}")
            .addQueryParameter("days", "14")
            .addQueryParameter("hour", "12")
            .addQueryParameter("alerts", "no")
            .addQueryParameter("aqi", "no")
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

        val weatherapiResponse = gson().fromJson(response.body?.string(), WeatherapiResponse::class.java)
        return ResponseParser.parse(weatherapiResponse)
    }
}
