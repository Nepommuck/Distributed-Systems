package edu.agh.cs.distributedsystems.weatherapi.model

import java.time.LocalDate


sealed interface SummaryWeatherApiResponse {
    data class Error(val message: String) : SummaryWeatherApiResponse
}

class SummaryWeather(
    val dailyForecasts: Map<LocalDate, DailyWeather>,
) : SummaryWeatherApiResponse

class DailyWeather(
    val dailyTotal: TemperatureData,
    val allData: List<TemperatureData>,
)
