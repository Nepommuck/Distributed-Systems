package edu.agh.cs.distributedsystems.weatherapi.model

import java.time.LocalDate


data class Temperature(val celsius: Double)

class TemperatureData(
    val averageTemperature: Temperature,
    val minTemperature: Temperature,
    val maxTemperature: Temperature,
)

class Weather(val dailyTemperatures: Map<LocalDate, TemperatureData>)
