package edu.agh.cs.distributedsystems.wetherapi.integrations.config

import edu.agh.cs.distributedsystems.wetherapi.persistence.MockPersistenceManager
import edu.agh.cs.distributedsystems.wetherapi.persistence.PersistenceManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PersistenceConfig {

    @Bean
    fun persistenceManager(): PersistenceManager = MockPersistenceManager()
}
