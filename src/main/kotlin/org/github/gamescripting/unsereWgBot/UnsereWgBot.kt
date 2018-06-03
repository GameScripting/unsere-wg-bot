package org.github.gamescripting.unsereWgBot

import com.fasterxml.jackson.databind.ObjectMapper
import org.github.gamescripting.unsereWgBot.models.Person
import org.github.gamescripting.unsereWgBot.network.Fritzbox
import org.github.gamescripting.unsereWgBot.reposiories.PeopleRepository
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Flag
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.CallbackQuery
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.function.Consumer
import java.util.function.Predicate
import javax.transaction.Transactional

val BOT_TOKEN = System.getenv("BOT_TOKEN")

private val mapper = ObjectMapper()


@Component
class UnsereWgBot(
        val fritzbox: Fritzbox,
        val peopleRepository: PeopleRepository
) : AbilityBot(BOT_TOKEN, "unser_wg_bot") {

    companion object {
    }

    override fun creatorId(): Int {
        return -837653572
    }

    var lastSelectedMacAdress: String? = null

    fun werIstDax() = defaultAllPublicAbility("weristda", "Zeigt an, wer gerade da ist").action { messageContext ->
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

    }.build()

    fun keyboardTest() = defaultAllPublicAbility("leute", "Erlaubt das hinzufügen eines neuen Namen")
            .action {
                val x = 5
            }
            .reply(Consumer { update ->
                val macAdresses = fritzbox.getMacAdressesInNetwork()
                val allPeople = peopleRepository.findAll()

                val message = update.message ?: return@Consumer

                val buttons = macAdresses.map { macAdress ->
                    val person = allPeople.firstOrNull { it.macAdress == macAdress }

                    if (person == null) {
                        InlineKeyboardButton().apply {
                            text = macAdress
                            callbackData = macAdress
                        }
                    } else {
                        InlineKeyboardButton().apply {
                            callbackData = macAdress
                            text = person.displayName
                        }
                    }
                }

                val keyboard = buttons.windowed(2, 2, true)
                val keyboardMarkup = InlineKeyboardMarkup().apply { this.keyboard = keyboard }

                val sendMessage = SendMessage().apply {
                    chatId = message.chatId.toString()
                    text = "Bitte wähle eine Person aus:"
                    replyMarkup = keyboardMarkup
                }

                silent.execute(sendMessage)
            }, Predicate { it?.message?.text == "/leute" })
            .reply(Consumer { update ->
                val callbackQuery = update.callbackQuery ?: return@Consumer
                val message = callbackQuery.message ?: return@Consumer

                lastSelectedMacAdress = callbackQuery.data

                silent.send("Wie lautet der Name?", message.chatId)
            }, Flag.CALLBACK_QUERY)
            .reply(Consumer { update ->
                val message = update.message ?: return@Consumer
                val macAdress = lastSelectedMacAdress ?: return@Consumer

                val existingUser = peopleRepository.findByMacAdress(macAdress)
                val newName = message.text
                val updatedUser = if (existingUser != null) {
                    existingUser.displayName = newName
                    existingUser
                } else {
                    Person().apply {
                        this.macAdress = macAdress
                        this.displayName = newName
                    }
                }

                savePerson(updatedUser)

                silent.send("Alles klar, wir haben ${updatedUser.displayName} gespeichert.", message.chatId)
            }, Predicate { it?.message?.text?.startsWith("/") == false })
            .build()

    @Transactional
    fun savePerson(person: Person) {
        peopleRepository.saveAndFlush(person)
    }
}