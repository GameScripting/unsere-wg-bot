package org.github.gamescripting.unsereWgBot.network

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("fritztr064")
class FritzTR064Config {
    var address: String? = null
    var user: String? = null
    var password: String? = null
}