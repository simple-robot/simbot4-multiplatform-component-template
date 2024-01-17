plugins {
    // Kotlin 的版本已经在 buildSrc 中引入了，因此此处可以省略
    kotlin("multiplatform")
    // 参考 https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin
    // 因为已经在 buildSrc 中添加了依赖，此处可以省略版本
    id("love.forte.plugin.suspend-transform")
}

group = "com.example.component.foo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Java 的编译配置
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "11"
    targetCompatibility = "11"

    // 此处配置 Kotlin 可以编译到你的 jvmMain/java 中的 module-info 文件
    // 将 'com.example.component.foo' 改成你真正的模块名称
    // 参考：https://kotlinlang.org/docs/gradle-configure-project.html#configure-with-java-modules-jpms-enabled
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
        listOf("--patch-module", "com.example.component.foo=${sourceSets["main"].output.asPath}")
    })
}

kotlin {
    //explicitApi() // 作为库的组件建议开启严格模式
    applyDefaultHierarchyTemplate()

    jvmToolchain(11)
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                javaParameters = true
                freeCompilerArgs = freeCompilerArgs + listOf("-Xjvm-default=all")
            }
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js()

    // 以下是 simbot-api 支持的 native 目标
    // tair1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // tair2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // tair3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.simbot.api)
        }

        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test")
            api(libs.simbot.core)
        }

    }
}


// 挂起函数的转化器插件，
// 可以用来将 `suspend fun xxx` 转化为 `fun xxxBlocking、xxxAsync` 等。
// 参考 https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin
// 如果最终的成品内没有挂起函数，可以考虑把这个插件以及 buildSrc 内相关的东西移除。
// suspendTransform {
//     includeRuntime = false
//     includeAnnotation = false
//
//     addJvmTransformers(
//         // @JvmBlocking
//         SuspendTransforms.jvmBlockingTransformer,
//         // @JvmAsync
//         SuspendTransforms.jvmAsyncTransformer,
//
//         // @JvmSuspendTrans
//         SuspendTransforms.suspendTransTransformerForJvmBlocking,
//         SuspendTransforms.suspendTransTransformerForJvmAsync,
//         SuspendTransforms.suspendTransTransformerForJvmReserve,
//
//         // @JvmSuspendTransProperty
//         SuspendTransforms.jvmSuspendTransPropTransformerForBlocking,
//         SuspendTransforms.jvmSuspendTransPropTransformerForAsync,
//         SuspendTransforms.jvmSuspendTransPropTransformerForReserve,
//     )
// }
