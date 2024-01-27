package com.example.component.foo.bot

import com.example.component.foo.FooComponent
import com.example.component.foo.event.FooEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import love.forte.simbot.bot.*
import love.forte.simbot.common.coroutines.mergeWith
import love.forte.simbot.common.function.ConfigurerFunction
import love.forte.simbot.common.function.invokeBy
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.NoSuchComponentException
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.plugin.PluginConfigureContext
import love.forte.simbot.plugin.PluginFactory
import love.forte.simbot.plugin.PluginFactoryProvider
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * `Foo` 组件库中用于示例的对 [BotManager] 的实现，
 * 用于提供一些与 bot 相关功能。
 *
 * 此处 [FooBotManager] 作为示例内容，它的功能是：
 * - 可以注册、管理 [FooBot]。
 * - 每一个被创建的 [FooBot]，都会周期性定时（可配置）推送事件 [FooEvent]
 *
 * 需要注意的内容：
 * - 作为简单示例，[FooBotManager] 暂时不考虑并发问题。
 *   但是实际开发中，我们强烈建议保证任何操作的并发安全。
 * - 作为简单示例，[FooBot] 可以被重复注册。
 * - 虽然示例中 [FooBotManager] 和 [FooBot] 都是普通的类，
 *   但是实际开发中，我们建议**对外提供接口，隐藏内部实现**。
 *
 *
 * @author ForteScarlet
 */
class FooBotManager
/*
 * 构造为 private，
 * 因为我们希望它只通过工厂被构建，
 * 并且构造参数也有一些要求需要被保证。
 */
private constructor(
    /**
     * 通过工厂构建时得到的组件信息。
     */
    private val component: FooComponent,
    /**
     * 协程作用域，应该包含 Job。
     * `coroutineContext` 会作为被构建出来的每一个 [FooBot] 的基础作用域。
     */
    private val coroutineContext: CoroutineContext,
    /**
     * 事件处理器，用于提供给 bot，让它们可以推送事件。
     */
    private val eventProcessor: EventProcessor
) : JobBasedBotManager() {
    // 此处实现了一个辅助的抽象类 JobBasedBotManager
    // 它会基于一个 Job 替你实现部分内容
    override val job: Job = coroutineContext[Job]!!

    /**
     * 已经注册了的所有 bot 的列表。
     */
    private val botList = mutableListOf<FooBot>()

    /**
     * 判断配置类型是否支持。此处要判断它是不是 [FooBotConfiguration]。
     */
    override fun configurable(configuration: SerializableBotConfiguration): Boolean {
        return configuration is FooBotConfiguration
    }

    /**
     * 使用通用类型注册一个 bot。
     * 如果类型不符合预期，抛出 [UnsupportedBotConfigurationException]。
     */
    override fun register(configuration: SerializableBotConfiguration): FooBot {
        val fooBotConfig = configuration as? FooBotConfiguration
            ?: throw UnsupportedBotConfigurationException("Configuration type mismatch")

        // 创建一个 bot
        return registerFoo(fooBotConfig)
    }

    /**
     * 一般我们建议针对 bot 提供一些专属的注册方法。
     */
    fun registerFoo(config: FooBotConfiguration? = null): FooBot {
        // 创建一个 bot
        return FooBot(
            component,
            config ?: FooBotConfiguration(),
            coroutineContext = coroutineContext + SupervisorJob(job),
            eventProcessor
        )
    }


    /**
     * 所有 bot
     */
    override fun all(): Sequence<Bot> = botList.asSequence()

    /**
     * 指定 id 的bot
     * 因为此示例中，bot 可以重复，因此如果有相同id的多个 bot，
     * 抛出异常 [ConflictBotException]。
     */
    override fun get(id: ID): Bot {
        val bots = all().filter { it.id == id }
            .take(2) // 只取最多两个即可
            .toList()

        when (bots.size) {
            // 数量正好
            1 -> return bots.first()
            // 没找到
            0 -> throw NoSuchBotException(id.literal)
            // 多了
            else -> throw ConflictBotException(id.literal)
        }
    }

    /**
     * [FooBotManager] 的工厂，跟 Plugin 一样，建议使用伴生对象实现。
     */
    companion object Factory : PluginFactory<FooBotManager, FooBotManagerConfiguration> {
        /**
         * 工厂的唯一标识，需要是一个单例对象，此处使用内部匿名实现它，
         */
        override val key: BotManagerFactory.Key = object : BotManagerFactory.Key {}

        override fun create(
            context: PluginConfigureContext,
            configurer: ConfigurerFunction<FooBotManagerConfiguration>
        ): FooBotManager {
            // 寻找 fooComponent 是否被注册了
            // 严谨一点的校验行为，如果没有对应的组件信息，应当抛出异常 NoSuchComponentException
            // 可以使用类型或id寻找
            val component = context.components.find { it is FooComponent } as? FooComponent
                ?: throw NoSuchComponentException(FooComponent.ID_VALUE)

            // 配置，并得到最终的配置类
            val config = FooBotManagerConfiguration().invokeBy(configurer)

            val appCoroutineContext = context.applicationConfiguration.coroutineContext
            val coroutineContext = config.coroutineContext

            // 此处要对 config 中的协程上下文进行一定的处理，主要是 Job
            // 因为如果配置类和 appCoroutineContext 中都存在 Job，
            // 那么，以配置类的 Job 作为 parent，但是app的job如果结束了也要影响到当前。
            // 此处可以使用 mergeWith，其结果中也必定包含一个 Job
            val mergedContext = coroutineContext.mergeWith(appCoroutineContext)

            // 创建实例
            return FooBotManager(component, mergedContext, context.eventDispatcher)
        }
    }
}

/**
 * 用于 [FooBotManager] 的配置类。此处支持配置协程上下文信息。
 */
class FooBotManagerConfiguration {
    var coroutineContext: CoroutineContext = EmptyCoroutineContext
}

/**
 * 跟 Plugin 一样，
 * 用来支持 SPI 自动加载的 [PluginFactoryProvider] 实现，
 * 对应信息添加在
 * `jvmMain/resources/META-INF/services/love.forte.simbot.plugin.PluginFactoryProvider`
 * 中。
 */
class FooBotManagerFactoryProvider : PluginFactoryProvider<FooBotManagerConfiguration> {
    override fun provide(): PluginFactory<*, FooBotManagerConfiguration> = FooBotManager
}
