package edu.agh.cs.distributedsystems.weatherapi.integrations.config

import edu.agh.cs.distributedsystems.weatherapi.persistence.MockPersistenceManager
import edu.agh.cs.distributedsystems.weatherapi.persistence.PersistenceManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PersistenceConfig {

    @Bean
    fun persistenceManager(): PersistenceManager = MockPersistenceManager()
}
