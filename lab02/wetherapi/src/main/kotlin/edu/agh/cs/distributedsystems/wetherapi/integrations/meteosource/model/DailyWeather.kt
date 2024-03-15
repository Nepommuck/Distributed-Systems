package edu.agh.cs.distributedsystems.wetherapi.integrations.meteosource.model

import java.util.*

class DailyWeather(
    val day: Date,
    val all_day: DailyDetails,
)


class DailyDetails(
    val temperature: Float,
    val temperature_min: Float,
    val temperature_max: Float,
)
