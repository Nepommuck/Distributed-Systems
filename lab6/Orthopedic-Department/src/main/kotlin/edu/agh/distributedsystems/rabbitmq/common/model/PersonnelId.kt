package edu.agh.distributedsystems.rabbitmq.common.model

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.random.Random

@Serializable
class PersonnelId(val value: Short) {
    companion object {
        internal fun provideFreeId(): PersonnelId =
            PersonnelId(
                value = abs(Random.nextInt().toShort().toInt()).toShort()
            )
    }
}

abstract class MedicalPersonnel {
    val personnelId = PersonnelId.provideFreeId()
}
