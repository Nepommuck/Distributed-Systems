package edu.agh.cs.distributedsystems.wetherapi.apis;


import edu.agh.cs.distributedsystems.wetherapi.model.Coordinates
import edu.agh.cs.distributedsystems.wetherapi.model.Location
import edu.agh.cs.distributedsystems.wetherapi.model.LocationApiResponse
import edu.agh.cs.distributedsystems.wetherapi.persistence.AddLocationResult
import edu.agh.cs.distributedsystems.wetherapi.persistence.DeleteLocationResult
import edu.agh.cs.distributedsystems.wetherapi.persistence.ModifyLocationResult
import edu.agh.cs.distributedsystems.wetherapi.persistence.PersistenceManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/locations")
class LocationsAPI(private val persistenceManager: PersistenceManager) {

    @GetMapping
    fun getLocations(): List<Location> = persistenceManager.getAllLocations()

    @GetMapping("/{id}")
    fun findLocationById(@PathVariable id: Int): ResponseEntity<LocationApiResponse> =
        when (val location = persistenceManager.findLocationById(id)) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(LocationApiResponse.Error("Location with id '$id' not found"))

            else -> ResponseEntity.ok(location)
        }

    @GetMapping("/name")
    fun findLocationByName(name: String): ResponseEntity<LocationApiResponse> =
        when (val location = persistenceManager.findLocationByName(name)) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(LocationApiResponse.Error("Location with name '$name' not found"))

            else -> ResponseEntity.ok(location)
        }

    @PostMapping
    fun addLocation(name: String, coordinates: Coordinates): ResponseEntity<LocationApiResponse> =
        when (val result = persistenceManager.insertNewLocation(name, coordinates)) {
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

    @PutMapping("/{id}")
    fun modifyLocation(
        @PathVariable("id") locationId: Int,
        newName: String,
        newCoordinates: Coordinates?,
    ): ResponseEntity<LocationApiResponse> =
        when (val result = persistenceManager.modifyExistingLocation(locationId, newName, newCoordinates)) {
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

    @DeleteMapping("/{id}")
    fun deleteLocation(@PathVariable("id") id: Int): ResponseEntity<LocationApiResponse> =
        when (val result = persistenceManager.deleteLocation(id)) {
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
