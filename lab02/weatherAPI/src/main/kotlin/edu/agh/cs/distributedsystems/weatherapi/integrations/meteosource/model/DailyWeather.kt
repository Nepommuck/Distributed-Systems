package edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource.model

import java.time.LocalDate

class DailyWeather(
    val day: LocalDate,
    val all_day: DailyDetails,
)


class DailyDetails(
    val temperature: Double,
    val temperature_min: Double,
    val temperature_max: Double,
)
