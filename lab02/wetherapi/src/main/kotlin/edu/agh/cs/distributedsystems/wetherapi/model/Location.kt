package edu.agh.cs.distributedsystems.wetherapi.model

import kotlin.random.Random

sealed interface LocationApiResponse {
    data class Error(val message: String) : LocationApiResponse
}

class Location(
    val id: Int = Random.nextInt(),
    val name: String,
    val coordinates: Coordinates,
) : LocationApiResponse