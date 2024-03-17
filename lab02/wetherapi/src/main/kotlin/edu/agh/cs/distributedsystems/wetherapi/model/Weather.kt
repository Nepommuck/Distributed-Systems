package edu.agh.cs.distributedsystems.wetherapi.model

import java.util.*


data class Temperature(val celsius: Double)

class TemperatureData(
    val averageTemperature: Temperature,
    val minTemperature: Temperature,
    val maxTemperature: Temperature,
)

class Weather(val dailyTemperatures: Map<Date, TemperatureData>)
