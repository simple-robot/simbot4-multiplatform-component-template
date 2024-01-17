package com.example.component.foo

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import love.forte.simbot.common.function.ConfigurerFunction
import love.forte.simbot.common.function.invokeBy
import love.forte.simbot.component.Component
import love.forte.simbot.component.ComponentConfigureContext
import love.forte.simbot.component.ComponentFactory
import kotlin.jvm.JvmField


/**
 *
 * @author ForteScarlet
 */
class FooComponent : Component {
    override val id: String get() = ID_VALUE
    override val serializersModule: SerializersModule get() = Factory.SerializersModule

    /** 伴生对象实现的工厂实现 */
    companion object Factory : ComponentFactory<FooComponent, FooComponentConfiguration> {
        /** id 常量化 */
        const val ID_VALUE: String = "com.example.foo"
        /** serializersModule "静态化" */
        @JvmField
        val SerializersModule: SerializersModule = EmptySerializersModule()

        override val key: ComponentFactory.Key = object : ComponentFactory.Key {}
        override fun create(context: ComponentConfigureContext, configurer: ConfigurerFunction<FooComponentConfiguration>): FooComponent {
            FooComponentConfiguration().invokeBy(configurer)
            return FooComponent()
        }
    }
}

class FooComponentConfiguration
