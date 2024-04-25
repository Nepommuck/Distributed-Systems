plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.google.protobuf:protobuf-java:4.26.1")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("io.grpc:grpc-protobuf:1.62.2")
    implementation("io.grpc:grpc-netty:1.62.2")
    implementation("io.grpc:grpc-netty-shaded:1.62.2")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

tasks.test {
    useJUnitPlatform()
}