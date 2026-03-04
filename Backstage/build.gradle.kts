plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Input handling for controllers - JInput
    implementation("net.java.jinput:jinput:2.0.10")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Networking - for Raspberry Pi communication
    implementation("commons-io:commons-io:2.11.0")

    // JSON for network protocol
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


tasks.test {
    useJUnitPlatform()
}