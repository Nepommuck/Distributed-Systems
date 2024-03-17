package edu.agh.cs.distributedsystems.weatherapi.integrations

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Weather
import edu.agh.cs.distributedsystems.weatherapi.util.LocalDateDeserializer
import edu.agh.cs.distributedsystems.weatherapi.util.LocalDateSerializer
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.time.LocalDate
import javax.naming.ConfigurationException


abstract class WeatherApiIntegration(url: String) {
    protected val baseUrl = url.toHttpUrlOrNull() ?: throw ConfigurationException("Invalid url configured")

    protected fun gson(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateSerializer())
        .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
        .create()

    abstract fun getWeatherFor(coordinates: Coordinates): Weather?
}
