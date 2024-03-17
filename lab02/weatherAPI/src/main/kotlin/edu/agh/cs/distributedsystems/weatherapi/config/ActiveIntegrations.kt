package edu.agh.cs.distributedsystems.weatherapi.config

import edu.agh.cs.distributedsystems.weatherapi.integrations.WeatherApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource.MeteosourceApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.weatherapi.WeatherapiApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.weatherbit.WeatherbitApiIntegration

object ActiveIntegrations {
    val integrations: List<WeatherApiIntegration> = listOf(
        MeteosourceApiIntegration(),
        WeatherbitApiIntegration(),
        WeatherapiApiIntegration(),
    )
}