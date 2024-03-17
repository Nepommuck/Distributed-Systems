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

            else -> ResponseEntity.ok(WeatherCentral.getWeather(coordinates))
        }
}