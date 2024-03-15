package edu.agh.cs.distributedsystems.wetherapi.integrations

import edu.agh.cs.distributedsystems.wetherapi.model.Coordinates
import edu.agh.cs.distributedsystems.wetherapi.model.Weather
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.naming.ConfigurationException


abstract class WeatherApiIntegration(url: String) {
    protected val baseUrl = url.toHttpUrlOrNull() ?: throw ConfigurationException("Invalid url configured")

    abstract fun getWeatherFor(coordinates: Coordinates): Weather?
}
