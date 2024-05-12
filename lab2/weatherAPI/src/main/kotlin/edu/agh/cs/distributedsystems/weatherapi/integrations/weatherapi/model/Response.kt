package edu.agh.cs.distributedsystems.weatherapi.integrations.weatherapi.model

import java.time.LocalDate

class WeatherapiResponse(
    val forecast: Forecast,
)

class Forecast(
    val forecastday: List<DailyWeather>,
)

class DailyWeather(
    val date: LocalDate,
    val day: DailyDetails,
)

class DailyDetails(
    val avgtemp_c: Double,
    val mintemp_c: Double,
    val maxtemp_c: Double,
)
