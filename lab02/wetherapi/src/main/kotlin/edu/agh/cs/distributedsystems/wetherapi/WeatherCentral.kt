package edu.agh.cs.distributedsystems.wetherapi

import edu.agh.cs.distributedsystems.wetherapi.integrations.WeatherApiIntegration
import edu.agh.cs.distributedsystems.wetherapi.integrations.meteosource.MeteosourceApiIntegration
import edu.agh.cs.distributedsystems.wetherapi.model.Coordinates
import edu.agh.cs.distributedsystems.wetherapi.model.SummaryWeather

object WeatherCentral {
    private val integrations: List<WeatherApiIntegration> = listOf(
        MeteosourceApiIntegration(),
    )

    fun getWeather(coordinates: Coordinates): SummaryWeather = SummaryWeather(
        integrations
            .mapNotNull { it.getWeatherFor(coordinates) }
            .flatMap { it.dailyTemperatures.toList() }
            .groupBy { (date, _) -> date }
            .mapValues { (_, listOfPairs) ->
                listOfPairs.map { (_, temperature) -> temperature }
            }
    )
}
