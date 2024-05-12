package edu.agh.cs.distributedsystems.weatherapi.persistence

import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Location
import org.springframework.stereotype.Component


sealed interface AddLocationResult {
    data class Success(val newLocation: Location) : AddLocationResult
    data object NameAlreadyExists : AddLocationResult
    data class UnknownError(val message: String) : AddLocationResult
}

sealed interface ModifyLocationResult {
    data class Success(val modifiedLocation: Location) : ModifyLocationResult
    data object NotFound : ModifyLocationResult
    data object NameAlreadyExists : ModifyLocationResult
    data class UnknownError(val message: String) : ModifyLocationResult
}

sealed interface DeleteLocationResult {
    data class Success(val deletedLocation: Location) : DeleteLocationResult
    data object NotFound : DeleteLocationResult
    data class UnknownError(val message: String) : DeleteLocationResult
}

@Component
interface PersistenceManager {
    fun getAllLocations(): List<Location>
    fun findLocationById(id: Int): Location?
    fun findLocationByName(name: String): Location?

    fun insertNewLocation(name: String, coordinates: Coordinates): AddLocationResult

    fun modifyExistingLocation(
        locationId: Int,
        newName: String? = null,
        newCoordinates: Coordinates? = null,
    ): ModifyLocationResult

    fun deleteLocation(locationId: Int): DeleteLocationResult
}