package com.example.component.foo.bot

import com.example.component.foo.FooComponent
import com.example.component.foo.event.FooEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.annotations.ExperimentalSimbotAPI
import love.forte.simbot.bot.*
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.UUID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.definition.ChatGroup
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.Guild
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.event.onEachError
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.logger.logger
import love.forte.simbot.suspendrunner.ST
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * `Foo` 组件库中用于示例的对 [Bot] 的实现，
 *  * 用于提供一些与 bot 相关功能。
 * @author ForteScarlet
 */
class FooBot(
    /**
     * FooBot 的组件标识，也就是 FooComponent
     */
    override val component: FooComponent,
    /**
     * bot 所需的配置信息。
     */
    private val configuration: FooBotConfiguration,
    /**
     * 协程上下文，应当包含一个 Job
     */
    coroutineContext: CoroutineContext,
    /**
     * 事件处理器，周期性的事件就是往这里面推送的。
     */
    private val eventProcessor: EventProcessor,
) : JobBasedBot() {
    // 此处实现了一个用于辅助的抽象类 JobBasedBot
    // 它会基于一个 Job 替你实现部分内容

    // 用于示例的 FooBot，id和name是随机的。
    override val id: ID = UUID.random()
    override val name: String = "FooBot($id)"


    override val coroutineContext: CoroutineContext
    override val job: Job

    init {
        // 对 coroutineContext 和 job 稍作处理，
        // 如果 coroutineContext 中没有 Job，创建一个。
        val job = coroutineContext[Job]
        if (job == null) {
            val newJob = SupervisorJob()
            this.job = newJob
            this.coroutineContext = coroutineContext + newJob
        } else {
            this.job = job
            this.coroutineContext = coroutineContext
        }
    }

    /**
     * 开始周期性的推送事件。
     * 事件的推送基于 [coroutineContext] 在异步中进行，
     * 当 bot 被关闭，异步任务也就被关闭了，不需要保存 job。
     */
    @OptIn(ExperimentalSimbotAPI::class)
    private fun initPushJob() {
        val duration = configuration.duration
        launch {
            while (true) {
                delay(duration)
                // 构建一个 event
                val event = FooEvent(this@FooBot, UUID.random(), Timestamp.now())
                // 推送事件，并收集结果
                // 此处是直接进行 collect 的，
                // 这样下一次开始推送事件的时间就是 本次处理的总时间 + duration
                // 如果想要严格保证 duration，则 push 行为也应该放在异步中。
                eventProcessor.push(event)
                    // 建议在出现任何事件处理结果为错误的时候，
                    // 做一些操作，例如输出日志
                    .onEachError { errorResult ->
                        logger.error(
                            "Error while pushing event {}: {}",
                            event,
                            errorResult.content.message,
                            errorResult.content
                        )
                    }
                    // 别忘了收集结果，否则推送的事件不会被真正处理。
                    .collect()
            }
        }
    }

    /**
     * 判断 [id] 是否为当前 bot 的 id。
     * 由于 [FooBot] 没有特殊机制或其他后初始化的id，因此直接与真正的 id 比较即可。
     */
    override fun isMe(id: ID): Boolean =
        this.id == id


    @Volatile
    private var started = false
    private val startLock = Mutex()

    /**
     * 启动 [FooBot]，也就是开始周期性的发送事件。
     * 此处会使用 [started] 判断，如果启动好了，就不再启动了。
     *
     * 此处使用 [Mutex] 加了锁来确保并发安全。
     *
     * 这是一个挂起函数，并且它的父函数有标记 @[ST]，
     * 因此建议增加 @[JvmSynthetic] 对 Java 隐藏挂起函数本身。
     * 当然，也可以同样标记一个 @[ST]，效果类似。
     *
     */
    @JvmSynthetic
    override suspend fun start() {
        startLock.withLock {
            if (started) {
                return
            }

            initPushJob()
            started = true
        }
    }

    /**
     * 针对 [Contact] 相关的实现。此处作为示例暂不考虑支持，
     * 不支持的情况下可直接返回 `null`。
     */
    override val contactRelation: ContactRelation? = null

    /**
     * 针对 [ChatGroup] 相关的实现。此处作为示例暂不考虑支持，
     * 不支持的情况下可直接返回 `null`。
     */
    override val groupRelation: GroupRelation? = null

    /**
     * 针对 [Guild] 相关的实现。此处作为示例暂不考虑支持，
     * 不支持的情况下可直接返回 `null`。
     */
    override val guildRelation: GuildRelation? = null

    companion object {
        /**
         * 一个logger
         */
        private val logger = LoggerFactory.logger<FooBot>()
    }

}

/**
 * bot 的配置类，实现 [SerializableBotConfiguration]
 * 并支持 [Serializable]，
 * 且不要忘了将它的多态信息添加到你的 Component 实现 [FooComponent] 中！
 *
 */
@Serializable
@SerialName(FooComponent.ID_VALUE)
class FooBotConfiguration : SerializableBotConfiguration() {
    var duration: Duration = 5.seconds
}
