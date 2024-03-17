package edu.agh.cs.distributedsystems.wetherapi.model

import java.util.*


sealed interface SummaryWeatherApiResponse {
    data class Error(val message: String) : SummaryWeatherApiResponse
}

class SummaryWeather(
    val dailyForecasts: Map<Date, DailyWeather>,
) : SummaryWeatherApiResponse

class DailyWeather(
    val dailyTotal: TemperatureData,
    val allData: List<TemperatureData>
)
