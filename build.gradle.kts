import love.forte.simbot.gradle.suspendtransforms.SuspendTransforms

plugins {
    // Kotlin和序列化插件
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
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
    js {
        nodejs()
        browser()
        binaries.library()
    }

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
            /*
             * 建议使用仅编译，
             * 因为一般我们会建议使用者手动添加具体的 simbot 核心库依赖。
             */
            compileOnly(libs.simbot.api)
            compileOnly(libs.simbot.common.annotations)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.simbot.core)
        }

        jsMain.dependencies {
            /*
             * compileOnly 在 js 平台和 native 平台上不怎么好使，
             * 所以非 JVM 的情况下需要改成 implementation 或 api
             */
            implementation(libs.simbot.api)
            implementation(libs.simbot.common.annotations)
        }

        nativeMain.dependencies {
            /*
             * compileOnly 在 js 平台和 native 平台上不怎么好使，
             * 所以非 JVM 的情况下需要改成 implementation 或 api
             */
            implementation(libs.simbot.api)
            implementation(libs.simbot.common.annotations)
        }

    }
}


// 挂起函数的转化器插件，
// 可以用来将 `suspend fun xxx` 转化为 `fun xxxBlocking、xxxAsync` 等。
// 参考 https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin
// 如果最终的成品内没有挂起函数，可以考虑把这个插件以及 buildSrc 内相关的东西移除。
//
// 此处的 SuspendTransforms 是在 buildSrc 中添加的 simbot.gradle.suspend
suspendTransform {
    includeRuntime = false
    includeAnnotation = false

    addJvmTransformers(
        // @JvmBlocking
        SuspendTransforms.jvmBlockingTransformer,
        // @JvmAsync
        SuspendTransforms.jvmAsyncTransformer,

        // @ST
        SuspendTransforms.suspendTransTransformerForJvmBlocking,
        SuspendTransforms.suspendTransTransformerForJvmAsync,
        SuspendTransforms.suspendTransTransformerForJvmReserve,

        // @STP
        SuspendTransforms.jvmSuspendTransPropTransformerForBlocking,
        SuspendTransforms.jvmSuspendTransPropTransformerForAsync,
        SuspendTransforms.jvmSuspendTransPropTransformerForReserve,
    )
}
