package edu.agh.cs.distributedsystems.wetherapi.integrations.meteosource.model

import java.util.*

class DailyWeather(
    val day: Date,
    val all_day: DailyDetails,
)


class DailyDetails(
    val temperature: Double,
    val temperature_min: Double,
    val temperature_max: Double,
)
