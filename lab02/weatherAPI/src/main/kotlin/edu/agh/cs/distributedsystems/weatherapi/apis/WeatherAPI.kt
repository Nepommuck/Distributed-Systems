package edu.agh.cs.distributedsystems.weatherapi.apis

import edu.agh.cs.distributedsystems.weatherapi.WeatherCentral
import edu.agh.cs.distributedsystems.weatherapi.model.SummaryWeatherApiResponse
import edu.agh.cs.distributedsystems.weatherapi.persistence.PersistenceManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/weather")
class WeatherAPI(val persistenceManager: PersistenceManager) {

    @GetMapping("/{locationName}")
    fun getWeather(@PathVariable locationName: String): ResponseEntity<SummaryWeatherApiResponse> =
        when (val coordinates = persistenceManager.findLocationByName(locationName)?.coordinates) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SummaryWeatherApiResponse.Error("Location with name '$locationName' not found"))

            // TODO
            else -> ResponseEntity.ok(WeatherCentral.getWeather(coordinates))
//            else -> ResponseEntity.ok(
//                SummaryWeather(
//                    mapOf(
//                        Pair(
//                            LocalDate.of(2024, 5, 11),
//                            DailyWeather(
//                                dailyTotal = TemperatureData(Temperature(15.5), Temperature(2.5), Temperature(20.9)),
//                                allData = listOf(
//                                    TemperatureData(Temperature(16.0), Temperature(5.5), Temperature(20.9)),
//                                    TemperatureData(Temperature(15.0), Temperature(2.5), Temperature(17.5)),
//                                )
//                            )
//                        ),
//                        Pair(
//                            LocalDate.of(2024, 5, 12),
//                            DailyWeather(
//                                dailyTotal = TemperatureData(Temperature(12.5), Temperature(5.5), Temperature(23.9)),
//                                allData = listOf(
//                                    TemperatureData(Temperature(16.0), Temperature(5.5), Temperature(20.9)),
//                                )
//                            )
//                        ),
//                    )
//                )
//            )
        }
}