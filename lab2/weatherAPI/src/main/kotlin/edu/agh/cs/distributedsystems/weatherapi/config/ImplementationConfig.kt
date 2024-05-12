package edu.agh.cs.distributedsystems.weatherapi.config

import edu.agh.cs.distributedsystems.weatherapi.persistence.MockPersistenceManager
import edu.agh.cs.distributedsystems.weatherapi.persistence.PersistenceManager
import edu.agh.cs.distributedsystems.weatherapi.security.ApiKeyValidator
import edu.agh.cs.distributedsystems.weatherapi.security.MockApiKeyValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ImplementationConfig {

    @Bean
    fun persistenceManager(): PersistenceManager = MockPersistenceManager()

    @Bean
    fun apiKeyValidator(): ApiKeyValidator = MockApiKeyValidator()
}
