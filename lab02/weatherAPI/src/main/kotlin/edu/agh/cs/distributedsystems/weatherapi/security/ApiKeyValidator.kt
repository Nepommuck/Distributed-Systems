package edu.agh.cs.distributedsystems.weatherapi.security

interface ApiKeyValidator {
    fun isKeyValid(key: String): Boolean
}