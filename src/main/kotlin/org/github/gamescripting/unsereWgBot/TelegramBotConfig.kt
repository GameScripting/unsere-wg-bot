package org.github.gamescripting.unsereWgBot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("telegrambot")
class TelegramBotConfig {
    var botToken: String? = null
    var botUsername: String? = null
    var creatorId: Int? = null
}