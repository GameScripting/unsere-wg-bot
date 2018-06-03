package org.github.gamescripting.unsereWgBot.network

import org.springframework.stereotype.Component

@Component
class Fritzbox {

    fun getMacAdressesInNetwork(): List<String> {
        return listOf(
                "AF-7B-A2-4D-55-E2",
                "F3-DC-EC-99-37-97",
                "26-C7-9D-32-72-24",
                "85-7F-20-B7-87-7D"
        )
    }
}