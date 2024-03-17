package edu.agh.cs.distributedsystems.weatherapi.util

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun Date.toLocalDate(): LocalDate {
    val instant = this.toInstant()
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}
