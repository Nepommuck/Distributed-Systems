package edu.agh.distributedsystems.rabbitmq.common.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Injury(val code: String) {
    @Serializable
    data object Hip : Injury("hip")

    @Serializable
    data object Knee : Injury("knee")

    @Serializable
    data object Elbow : Injury("elbow")

    companion object {
        fun parse(code: String): Injury? {
            return when (code.lowercase()) {
                Hip.code -> Hip
                Knee.code -> Knee
                Elbow.code -> Elbow
                else -> null
            }
        }
    }
}
