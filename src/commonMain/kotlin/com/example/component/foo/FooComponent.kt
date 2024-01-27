package com.example.component.foo

import com.example.component.foo.bot.FooBotConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass
import love.forte.simbot.bot.serializableBotConfigurationPolymorphic
import love.forte.simbot.common.function.ConfigurerFunction
import love.forte.simbot.common.function.invokeBy
import love.forte.simbot.component.Component
import love.forte.simbot.component.ComponentConfigureContext
import love.forte.simbot.component.ComponentFactory
import love.forte.simbot.component.ComponentFactoryProvider
import kotlin.jvm.JvmField


/**
 * 示例组件 `Foo` 的 **组件标识**。
 * @author ForteScarlet
 */
class FooComponent : Component {
    override val id: String get() = ID_VALUE
    override val serializersModule: SerializersModule get() = SerializersModule

    /**
     * [Component] 的工厂，
     * 建议使用伴生对象实现。
     *
     */
    companion object Factory : ComponentFactory<FooComponent, FooComponentConfiguration> {
        /**
         * 将 id 常量化，
         * 可以更友好的在 [FooComponent] 实例之外向其他人提供此信息。
         */
        const val ID_VALUE: String = "com.example.foo"
        /**
         * 将 serializersModule "静态化"，
         * 可以更友好的在 [FooComponent] 实例之外向其他人提供此信息。
         *
         */
        @JvmField
        val SerializersModule: SerializersModule = SerializersModule {
            // 此处添加 FooBot 配置类的序列化信息
            // 这样可以支持一些自动扫描的环境下（比如 Spring Boot）
            // 可以自动反序列化 bot 的配置文件。
            serializableBotConfigurationPolymorphic {
                subclass(FooBotConfiguration.serializer())
            }
        }

        /**
         * 组件的 [Factory] 的唯一标识，需要是一个单例对象，此处直接使用内部匿名实现。
         */
        override val key: ComponentFactory.Key = object : ComponentFactory.Key {}


        override fun create(context: ComponentConfigureContext, configurer: ConfigurerFunction<FooComponentConfiguration>): FooComponent {
            /**
             * 不论配置类是不是空类，都要尽可能保证 [configurer] 被调用过一次。
             */
            FooComponentConfiguration().invokeBy(configurer)

            /**
             * 最终构建出组件标识的实例结果。
             */
            return FooComponent()
        }
    }
}

/**
 * 供给组件的组件配置类。
 * 此示例中没有什么可配置的，因此是个空类。
 */
class FooComponentConfiguration

/**
 * 用来支持 SPI 自动加载的 [ComponentFactoryProvider] 实现，
 * 对应信息添加在
 * `jvmMain/resources/META-INF/services/love.forte.simbot.plugin.PluginFactoryProvider`
 * 中。
 *
 */
class FooComponentFactory : ComponentFactoryProvider<FooComponentConfiguration> {
    override fun provide(): ComponentFactory<*, FooComponentConfiguration> =
        FooComponent
}
