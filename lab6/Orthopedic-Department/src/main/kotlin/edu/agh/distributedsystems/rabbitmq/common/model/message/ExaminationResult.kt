package edu.agh.distributedsystems.rabbitmq.common.model.message

import edu.agh.distributedsystems.rabbitmq.common.model.Injury
import edu.agh.distributedsystems.rabbitmq.common.model.Patient
import kotlinx.serialization.Serializable

enum class ExaminationVerdict {
    OK,
    OPERATION_REQUIRED,
}

@Serializable
data class ExaminationResult(val patient: Patient, val injury: Injury, val verdict: ExaminationVerdict)
