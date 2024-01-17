package com.example.component.foo

import love.forte.simbot.common.function.ConfigurerFunction
import love.forte.simbot.common.function.invokeWith
import love.forte.simbot.component.NoSuchComponentException
import love.forte.simbot.component.find
import love.forte.simbot.event.EventResult
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.logger.logger
import love.forte.simbot.plugin.*


/**
 *
 * @author ForteScarlet
 */
class FooPlugin : Plugin {
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


            // 此处预留空间，实现功能
            val handle = context.eventDispatcher.register { eventContext ->
                logger.info("Event: {}", eventContext.event)

                EventResult.empty()
            }

            return FooPlugin()
        }
    }
}

class FooPluginConfiguration

class FooPluginFactoryProvider : PluginFactoryProvider<FooPluginConfiguration> {
    override fun configurersLoader(): Sequence<PluginFactoryConfigurerProvider<FooPluginConfiguration>>? {
        return null
    }

    override fun provide(): PluginFactory<*, FooPluginConfiguration> {
        return FooPlugin.Factory
    }
}

