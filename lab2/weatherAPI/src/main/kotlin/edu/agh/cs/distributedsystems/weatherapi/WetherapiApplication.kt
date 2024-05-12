package edu.agh.cs.distributedsystems.weatherapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WetherapiApplication

fun main(args: Array<String>) {
    runApplication<WetherapiApplication>(*args)
}
