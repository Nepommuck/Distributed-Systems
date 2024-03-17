package edu.agh.cs.distributedsystems.weatherapi.integrations.weatherbit.model

import java.time.LocalDate
import java.util.Date

class WeatherbitResponse(
    val data: List<DailyData>
)

class DailyData(
    val datetime: LocalDate,
    val temp: Double,
    val min_temp: Double,
    val max_temp: Double,
)