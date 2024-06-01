package edu.agh.distributedsystems.rabbitmq.common.model.message

import kotlinx.serialization.Serializable

@Serializable
data class PersonnelNotification(val message: String)
