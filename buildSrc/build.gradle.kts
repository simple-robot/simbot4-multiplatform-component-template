plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
}

val kotlinVersion = "1.9.21"
val suspendTransformVersion = "0.6.0-beta3"

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    // 用于使用插件 `id("love.forte.plugin.suspend-transform")`
    implementation("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:$suspendTransformVersion")
}

