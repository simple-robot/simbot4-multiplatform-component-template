package com.example.component.foo.event

import com.example.component.foo.bot.FooBot
import love.forte.simbot.bot.Bot
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.event.BotEvent


/**
 * 一个简单的 [BotEvent] 实现，
 * 没有实现其他特殊类型，用于在 [FooBot] 中定时推送。
 *
 * @author ForteScarlet
 */
open class FooEvent(override val bot: Bot, override val id: ID, override val time: Timestamp) : BotEvent
