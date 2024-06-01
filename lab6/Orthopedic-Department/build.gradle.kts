plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.5.0"
}

group = "edu.agh.distributedsystems.rabbitmq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.17.1")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
//    implementation("com.google.protobuf:protobuf-java:3.21.7")
//    implementation("com.google.protobuf:protobuf-java-util:3.18.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}