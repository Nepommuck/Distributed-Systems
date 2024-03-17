package edu.agh.cs.distributedsystems.weatherapi.apis


import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Location
import edu.agh.cs.distributedsystems.weatherapi.model.LocationApiResponse
import edu.agh.cs.distributedsystems.weatherapi.persistence.AddLocationResult
import edu.agh.cs.distributedsystems.weatherapi.persistence.DeleteLocationResult
import edu.agh.cs.distributedsystems.weatherapi.persistence.ModifyLocationResult
import edu.agh.cs.distributedsystems.weatherapi.persistence.PersistenceManager
import edu.agh.cs.distributedsystems.weatherapi.security.ApiKeyValidator
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/locations")
class LocationsAPI(private val persistenceManager: PersistenceManager, private val apiKeyValidator: ApiKeyValidator) {

    @GetMapping
    fun getLocations(key: String): ResponseEntity<List<Location>> = when (apiKeyValidator.isKeyValid(key)) {
        false ->
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)

        true ->
            ResponseEntity.ok(persistenceManager.getAllLocations())
    }

    @GetMapping("/{id}")
    fun findLocationById(@PathVariable id: Int, key: String): ResponseEntity<LocationApiResponse> =
        when (apiKeyValidator.isKeyValid(key)) {
            false -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(LocationApiResponse.Error("API key is not valid"))

            true -> when (val location = persistenceManager.findLocationById(id)) {
                null -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(LocationApiResponse.Error("Location with id '$id' not found"))

                else -> ResponseEntity.ok(location)
            }
        }


    @GetMapping("/name")
    fun findLocationByName(name: String, key: String): ResponseEntity<LocationApiResponse> =
        when (apiKeyValidator.isKeyValid(key)) {
            false -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(LocationApiResponse.Error("API key is not valid"))

            true -> when (val location = persistenceManager.findLocationByName(name)) {
                null -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(LocationApiResponse.Error("Location with name '$name' not found"))

                else -> ResponseEntity.ok(location)
            }
        }

    @PostMapping
    fun addLocation(name: String, coordinates: Coordinates, key: String): ResponseEntity<LocationApiResponse> =
        when (apiKeyValidator.isKeyValid(key)) {
            false -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(LocationApiResponse.Error("API key is not valid"))

            true -> when (val result = persistenceManager.insertNewLocation(name, coordinates)) {
                is AddLocationResult.NameAlreadyExists -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(LocationApiResponse.Error("Location named '$name' already exists"))

                is AddLocationResult.UnknownError -> ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LocationApiResponse.Error("DB returned an internal error: '${result.message}'"))

                is AddLocationResult.Success -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.newLocation)
            }
        }

    @PutMapping("/{id}")
    fun modifyLocation(
        @PathVariable("id") locationId: Int,
        newName: String,
        newCoordinates: Coordinates?,
        key: String,
    ): ResponseEntity<LocationApiResponse> =
        when (apiKeyValidator.isKeyValid(key)) {
            false -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(LocationApiResponse.Error("API key is not valid"))

            true -> when (
                val result = persistenceManager.modifyExistingLocation(locationId, newName, newCoordinates)
            ) {
                is ModifyLocationResult.NotFound -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(LocationApiResponse.Error("Location with id '$locationId' couldn't be found"))

                is ModifyLocationResult.NameAlreadyExists -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(LocationApiResponse.Error("Location with name '$newName' already exists"))

                is ModifyLocationResult.UnknownError -> ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LocationApiResponse.Error("DB returned an internal error: '${result.message}'"))

                is ModifyLocationResult.Success -> ResponseEntity
                    .ok(result.modifiedLocation)
            }
        }

    @DeleteMapping("/{id}")
    fun deleteLocation(@PathVariable("id") id: Int, key: String): ResponseEntity<LocationApiResponse> =
        when (apiKeyValidator.isKeyValid(key)) {
            false -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(LocationApiResponse.Error("API key is not valid"))

            true -> when (val result = persistenceManager.deleteLocation(id)) {
                is DeleteLocationResult.NotFound -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(LocationApiResponse.Error("Location with id '$id' couldn't be found"))

                is DeleteLocationResult.UnknownError -> ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LocationApiResponse.Error("DB returned an internal error: '${result.message}'"))

                is DeleteLocationResult.Success -> ResponseEntity
                    .ok(result.deletedLocation)
            }
        }
}
