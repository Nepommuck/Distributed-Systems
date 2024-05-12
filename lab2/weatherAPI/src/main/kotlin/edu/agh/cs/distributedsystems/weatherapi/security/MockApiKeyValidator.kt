package edu.agh.cs.distributedsystems.weatherapi.security

class MockApiKeyValidator : ApiKeyValidator {
    override fun isKeyValid(key: String): Boolean =
        key == "01g51w84535464"
}