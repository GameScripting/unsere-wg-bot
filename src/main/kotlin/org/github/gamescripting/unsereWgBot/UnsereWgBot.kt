package org.github.gamescripting.unsereWgBot

import org.github.gamescripting.unsereWgBot.models.Person
import org.github.gamescripting.unsereWgBot.network.Fritzbox
import org.github.gamescripting.unsereWgBot.reposiories.PeopleRepository
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.function.Consumer

@Component
class UnsereWgBot(
        val telegramBotConfig: TelegramBotConfig,
        val fritzbox: Fritzbox,
        val peopleRepository: PeopleRepository
) : AbilityBot(telegramBotConfig.botToken, telegramBotConfig.botUsername) {
    override fun creatorId() = telegramBotConfig.creatorId
            ?: throw Exception("Please set the telegramBot.creatorId in the config")

    companion object {
        val MESSAGE_WERISTDA = "/weristda"
        val MESSAGE_LEUTE = "/leute"
    }

    var lastIncomingMessage: String? = null
    var lastOutgoingMessageKey: MessageKey? = null
    var lastCallbackQueryData: String? = null

    fun default() = defaultAllPublicAbility("default", "Default handler").action(handleMessage()).build()
    fun leute() = defaultAllPublicAbility("leute", "Zeigt alle Geräte an").action(handleMessage()).build()
    fun weristda() = defaultAllPublicAbility("weristda", "Zeigt an wer da ist").action(handleMessage()).build()

    private fun handleMessage(): Consumer<MessageContext> = Consumer { messageContext ->
        val text = messageContext.update()?.message?.text
        val callbackQueryData = messageContext.update()?.callbackQuery?.data

        try {
            ensureWgGroup(messageContext) {
                when {
                    text?.startsWith(MESSAGE_WERISTDA, ignoreCase = true) == true -> {
                        showPeople(messageContext)
                    }
                    text?.startsWith(MESSAGE_LEUTE, ignoreCase = true) == true -> {
                        showPeopleWithKeyboard(messageContext)
                    }
                    callbackQueryData != null && lastIncomingMessage.equals(MESSAGE_LEUTE, ignoreCase = true) -> {
                        askForName(messageContext)
                        lastOutgoingMessageKey = MessageKey.ASKED_FOR_NAME
                    }
                    lastOutgoingMessageKey == MessageKey.ASKED_FOR_NAME && text != null -> {
                        addPerson(messageContext, text)
                        lastCallbackQueryData = null
                        lastOutgoingMessageKey = null
                    }
                    else -> showUnknownCommand(messageContext)
                }
            }
        } finally {
            if (text != null) {
                lastIncomingMessage = text
            }

            if (callbackQueryData != null) {
                lastCallbackQueryData = callbackQueryData
            }
        }
    }

    private fun addPerson(messageContext: MessageContext, text: String) {
        val newName = text
        val macAdress = lastCallbackQueryData ?: throw Exception("Invalid state, macAddress unknown")

        val existingUser = peopleRepository.findByMacAdress(macAdress)
        val updatedUser = if (existingUser != null) {
            existingUser.displayName = newName
            existingUser
        } else {
            Person().apply {
                this.macAdress = macAdress
                this.displayName = newName
            }
        }

        peopleRepository.saveAndFlush(updatedUser)

        silent.send("Alles klar, wir haben ${updatedUser.displayName} gespeichert.", messageContext.chatId())
    }

    private fun askForName(messageContext: MessageContext) {
        silent.send("Wie lautet der Name?", messageContext.chatId())
    }

    private fun showPeopleWithKeyboard(messageContext: MessageContext) {
        val networkDevices = fritzbox.readNetworkDevices()
        val allPeople = peopleRepository.findAll()

        val buttons = networkDevices.map { device ->
            val person = allPeople.firstOrNull { it.macAdress == device.macAddress }

            if (person == null) {
                InlineKeyboardButton().apply {
                    text = "${device.macAddress} (${device.hostName})"
                    callbackData = device.macAddress
                }
            } else {
                InlineKeyboardButton().apply {
                    callbackData = device.macAddress
                    text = person.displayName
                }
            }
        }

        val keyboard = buttons.windowed(2, 2, true)
        val keyboardMarkup = InlineKeyboardMarkup().apply { this.keyboard = keyboard }

        val sendMessage = SendMessage().apply {
            chatId = messageContext.chatId().toString()
            text = "Bitte wähle eine Person aus:"
            replyMarkup = keyboardMarkup
        }

        silent.execute(sendMessage)
    }

    private fun showUnknownCommand(messageContext: MessageContext) {
        silent.send("Unbekannter Befehl", messageContext.chatId())
        lastOutgoingMessageKey = MessageKey.ASKED_FOR_NAME
    }

    private fun showPeople(messageContext: MessageContext) {
        val macAdresses = fritzbox.getMacAdressesInNetwork()
        val allPeople = peopleRepository.findAll()

        val names = macAdresses.map { macAdress ->
            allPeople.firstOrNull { it.macAdress == macAdress }
        }.filterNotNull().map { it.displayName }

        if (names.any()) {
            val nameList = names.joinToString(separator = "\n") { "- $it" }
            silent.send("Im moment sind da:\n$nameList", messageContext.chatId())
        } else {
            silent.send("Im moment ist niemand da.", messageContext.chatId())
        }
    }

    fun sayHelloAfterStart() {
        for (groupId in telegramBotConfig.allowedGroupIds) {
            silent.send("Moin, ich bin dann wieder online 👋", groupId)
        }
    }
}

enum class MessageKey {
    ASKED_FOR_NAME
}