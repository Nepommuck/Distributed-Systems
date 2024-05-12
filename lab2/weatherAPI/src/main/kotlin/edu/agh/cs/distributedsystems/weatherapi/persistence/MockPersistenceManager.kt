package edu.agh.cs.distributedsystems.weatherapi.persistence

import edu.agh.cs.distributedsystems.weatherapi.model.Coordinates
import edu.agh.cs.distributedsystems.weatherapi.model.Location


private val dbInitialState = mutableListOf(
    Location(name = "Krakow", coordinates = Coordinates(lat = 50.0647, lon = 19.9450)),
    Location(name = "Warsaw", coordinates = Coordinates(lat = 52.2297, lon = 21.0122)),
    Location(name = "Prague", coordinates = Coordinates(lat = 50.0755, lon = 14.4378)),
)

class MockPersistenceManager : PersistenceManager {
    private val locations: MutableList<Location> = dbInitialState

    override fun getAllLocations(): List<Location> = locations.toList()

    override fun findLocationById(id: Int): Location? =
        locations.find { it.id == id }

    override fun findLocationByName(name: String): Location? =
        locations.find { it.name == name }

    override fun insertNewLocation(name: String, coordinates: Coordinates): AddLocationResult {
        if (findLocationByName(name) != null)
            return AddLocationResult.NameAlreadyExists

        val newLocation = Location(name = name, coordinates = coordinates)
        locations.add(newLocation)
        return AddLocationResult.Success(newLocation)
    }

    override fun modifyExistingLocation(
        locationId: Int,
        newName: String?,
        newCoordinates: Coordinates?,
    ): ModifyLocationResult =
        when (val location = findLocationById(locationId)) {
            null -> ModifyLocationResult.NotFound
            else -> {
                val locationWithNewName =
                    if (newName != null) findLocationByName(newName)
                    else null

                when (locationWithNewName) {
                    null -> {
                        deleteLocation(locationId)
                        val newLocation = Location(
                            id = locationId,
                            name = newName ?: location.name,
                            coordinates = newCoordinates ?: location.coordinates,
                        )
                        locations.add(newLocation)
                        ModifyLocationResult.Success(newLocation)
                    }

                    else -> ModifyLocationResult.NameAlreadyExists
                }
            }
        }

    override fun deleteLocation(locationId: Int): DeleteLocationResult =
        when (val location = findLocationById(locationId)) {
            null -> DeleteLocationResult.NotFound
            else -> {
                locations.remove(location)
                DeleteLocationResult.Success(location)
            }
        }
}
