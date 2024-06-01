package edu.agh.distributedsystems.rabbitmq.common.model.message

import edu.agh.distributedsystems.rabbitmq.common.model.Injury
import edu.agh.distributedsystems.rabbitmq.common.model.Patient
import kotlinx.serialization.Serializable

@Serializable
data class ExaminationRequest(val patient: Patient, val injury: Injury)
