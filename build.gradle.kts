plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.1"
}

group = "ao.sh1rocu"
version = "1.0.1"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
dependencies{
    implementation("net.mamoe:mirai-console:2.10.1");
    implementation("com.google.code.gson:gson:2.8.6");
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2-alpha1")
}