package com.example.component.foo

import love.forte.simbot.common.function.ConfigurerFunction
import love.forte.simbot.common.function.invokeWith
import love.forte.simbot.component.NoSuchComponentException
import love.forte.simbot.component.find
import love.forte.simbot.event.EventListenerRegistrationHandle
import love.forte.simbot.event.EventResult
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.logger.logger
import love.forte.simbot.plugin.Plugin
import love.forte.simbot.plugin.PluginConfigureContext
import love.forte.simbot.plugin.PluginFactory
import love.forte.simbot.plugin.PluginFactoryProvider


/**
 * 一个简易的 [Plugin] 实现，一般用于提供一些简单的功能，
 * 或者与 bot 不太相关的功能。
 *
 * [FooPlugin] 作为示例的功能是：
 * 注册一个事件处理器，并输出每一个事件到日志中。
 *
 * 如果是与 bot 相关的功能，更建议前往参考 [com.example.component.foo.bot.FooBotManager]。
 *
 * 如果要删除本实现，记得也要前往
 * `resources/META-INF/services/love.forte.simbot.plugin.Plugin`
 * 删除对应的引用。
 *
 *
 * @author ForteScarlet
 */
class FooPlugin(
    /**
     * 此处保存这个 handle，并使用 `public` 的访问级别，
     * 可用来允许使用者在后续取消注册的事件处理器。
     */
    val handle: EventListenerRegistrationHandle
) : Plugin {

    /**
     * [Plugin] 的工厂，建议使用伴生对象直接实现。
     */
    companion object Factory : PluginFactory<FooPlugin, FooPluginConfiguration> {
        private val logger = LoggerFactory.logger<FooPlugin>()

        override val key: PluginFactory.Key = object : PluginFactory.Key {}

        override fun create(
            context: PluginConfigureContext,
            configurer: ConfigurerFunction<FooPluginConfiguration>
        ): FooPlugin {
            configurer.invokeWith(FooPluginConfiguration())

            // 校验组件
            if (context.components.find<FooComponent>() == null) {
                throw NoSuchComponentException(FooComponent.ID_VALUE)
            }


            // 实现功能
            val handle = context.eventDispatcher.register {
                logger.info("Event: {}", event)

                EventResult.empty()
            }

            return FooPlugin(handle)
        }
    }
}

class FooPluginConfiguration

/**
 * 用来支持 SPI 自动加载的 [PluginFactoryProvider] 实现，
 * 对应信息添加在
 * `jvmMain/resources/META-INF/services/love.forte.simbot.plugin.PluginFactoryProvider`
 * 中。
 *
 */
class FooPluginFactoryProvider : PluginFactoryProvider<FooPluginConfiguration> {
    override fun provide(): PluginFactory<*, FooPluginConfiguration> {
        return FooPlugin.Factory
    }
}

