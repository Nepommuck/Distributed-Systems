package edu.agh.cs.distributedsystems.weatherapi

import edu.agh.cs.distributedsystems.weatherapi.integrations.WeatherApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.integrations.weatherbit.WeatherbitApiIntegration
import edu.agh.cs.distributedsystems.weatherapi.model.*
import kotlin.math.max
import kotlin.math.min

object WeatherCentral {
    private val integrations: List<WeatherApiIntegration> = listOf(
        WeatherbitApiIntegration(),
    )

    private fun calculateDailyTotal(allData: List<TemperatureData>): TemperatureData {
        fun loop(
            remainingData: List<TemperatureData>,
            count: Int = 0,
            averagesSum: Double = 0.0,
            min: Double = Double.MAX_VALUE,
            max: Double = Double.MIN_VALUE,
        ): TemperatureData = when (val newData = remainingData.firstOrNull()) {
            null -> TemperatureData(
                averageTemperature = Temperature(averagesSum / count),
                minTemperature = Temperature(min),
                maxTemperature = Temperature(max),
            )

            else -> loop(
                remainingData = remainingData.drop(1),
                count = count + 1,
                averagesSum = averagesSum + newData.averageTemperature.celsius,
                min = min(min, newData.minTemperature.celsius),
                max = max(max, newData.maxTemperature.celsius),
            )
        }
        return loop(remainingData = allData)
    }

    fun getWeather(coordinates: Coordinates): SummaryWeather = SummaryWeather(
        integrations
            .mapNotNull { it.getWeatherFor(coordinates) }
            .flatMap { it.dailyTemperatures.toList() }
            .groupBy { (date, _) -> date }
            .mapValues { (_, listOfPairs) ->
                val allData = listOfPairs.map { (_, temperature) -> temperature }
                DailyWeather(
                    dailyTotal = calculateDailyTotal(allData),
                    allData = allData,
                )
            }
    )
}
