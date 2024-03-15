package edu.agh.cs.distributedsystems.wetherapi.integrations.meteosource

import edu.agh.cs.distributedsystems.wetherapi.integrations.meteosource.model.MeteosourceResponse
import edu.agh.cs.distributedsystems.wetherapi.model.TemperatureCelsius
import edu.agh.cs.distributedsystems.wetherapi.model.TemperatureData
import edu.agh.cs.distributedsystems.wetherapi.model.Weather

object ResponseParser {
    fun parse(response: MeteosourceResponse): Weather = Weather(
        response.daily.data
            .associateBy { it.day }
            .mapValues { (_, dailyWeather) ->
                val data = dailyWeather.all_day
                TemperatureData(
                    averageTemperature = TemperatureCelsius(data.temperature),
                    minTemperature = TemperatureCelsius(data.temperature_min),
                    maxTemperature = TemperatureCelsius(data.temperature_max),
                )
            }
    )
}