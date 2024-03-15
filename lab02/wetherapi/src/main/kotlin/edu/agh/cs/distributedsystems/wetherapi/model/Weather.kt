package edu.agh.cs.distributedsystems.wetherapi.model

import java.util.*


class TemperatureCelsius(val value: Float)

class TemperatureData(
    val averageTemperature: TemperatureCelsius,
    val minTemperature: TemperatureCelsius,
    val maxTemperature: TemperatureCelsius,
)

class Weather(val dailyTemperatures: Map<Date, TemperatureData>)
