import com.example.component.foo.FooComponent
import com.example.component.foo.FooPlugin
import com.example.component.foo.bot.FooBotManager
import com.example.component.foo.event.FooEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import love.forte.simbot.application.listeners
import love.forte.simbot.core.application.launchSimpleApplication
import love.forte.simbot.event.process
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * 尝试加载插件，并处理 [FooEvent].
 *
 * @author ForteScarlet
 */
class TestMain {

    @Test
    fun runApplication() = runTest {
        val app = launchSimpleApplication {
            config {
                // 单元测试里不指定调度器，有些平台有概率会卡住
                coroutineContext = Dispatchers.Default
            }
            // findAndInstallAllComponents(true)
            // findAndInstallAllPlugins(true)
            // 上面这两个在 JVM 测试的时候才能用
            install(FooComponent)
            install(FooPlugin)
            install(FooBotManager)
        }

        app.listeners {
            process {
                println("On Event: $event")
                assertIs<FooEvent>(event)
                // 结束
                app.cancel()
            }
        }

        val botManger = app.botManagers.find { it is FooBotManager } as? FooBotManager
        assertNotNull(botManger)

        val bot = botManger.registerFoo()
        // 启动 bot
        bot.start()

        launch {
            delay(1000)
            bot.cancel()
            app.cancel()
        }

        // 挂起 app，直到被结束
        app.join()
    }

}
