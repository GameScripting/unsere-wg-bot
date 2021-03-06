package org.github.gamescripting.unsereWgBot

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi

@SpringBootApplication
class App(
        val unsereWgBot: UnsereWgBot
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        ApiContextInitializer.init()
        val telegramBotsApi = TelegramBotsApi()
        telegramBotsApi.registerBot(unsereWgBot)

        unsereWgBot.sayHelloAfterStart()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(App::class.java)
}