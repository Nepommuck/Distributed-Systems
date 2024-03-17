package edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource

import com.google.gson.Gson
import edu.agh.cs.distributedsystems.weatherapi.integrations.WeatherApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.config.ApiKeys
import edu.agh.cs.distributedsystems.weatherapi.integrations.config.ApiUrls
import edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource.model.MeteosourceResponse
import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Weather
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MeteosourceApiIntegration : WeatherApiIntegration(ApiUrls.meteosource) {
    private val key = ApiKeys.meteosource

    private fun requestDailyWeather(coordinates: Coordinates): Response {
        val url = baseUrl.newBuilder()
            .addQueryParameter("lat", coordinates.lat.toString())
            .addQueryParameter("lon", coordinates.lon.toString())
            .addQueryParameter("language", "en")
            .addQueryParameter("units", "metric")
            .addQueryParameter("sections", "daily")
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

        val metasourceResponse = Gson().fromJson(response.body?.string(), MeteosourceResponse::class.java)
        return ResponseParser.parse(metasourceResponse)
    }
}
