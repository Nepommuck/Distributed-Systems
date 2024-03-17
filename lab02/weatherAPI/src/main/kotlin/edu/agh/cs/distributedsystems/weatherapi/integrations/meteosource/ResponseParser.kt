package edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource

import edu.agh.cs.distributedsystems.weatherapi.integrations.meteosource.model.MeteosourceResponse
import edu.agh.cs.distributedsystems.weatherapi.model.Temperature
import edu.agh.cs.distributedsystems.weatherapi.model.TemperatureData
import edu.agh.cs.distributedsystems.weatherapi.model.Weather

object ResponseParser {
    fun parse(response: MeteosourceResponse): Weather = Weather(
        response.daily.data
            .associateBy { it.day }
            .mapValues { (_, dailyWeather) ->
                val data = dailyWeather.all_day
                TemperatureData(
                    averageTemperature = Temperature(data.temperature),
                    minTemperature = Temperature(data.temperature_min),
                    maxTemperature = Temperature(data.temperature_max),
                )
            }
    )
}