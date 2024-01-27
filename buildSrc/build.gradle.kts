plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
}

val kotlinVersion: String = libs.versions.kotlin.get()

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    // 用于使用插件 `id("love.forte.plugin.suspend-transform")`
    implementation(libs.suspend.transform.gradle)
    // 用于使用辅助的 SuspendTransforms 配置
    implementation(libs.simbot.gradle.suspend)
}

