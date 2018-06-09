package org.github.gamescripting.unsereWgBot

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.api.objects.Message
import java.util.*

fun <T> Optional<T>.asNullable(): T? {
    if (!this.isPresent) {
        return null
    }

    return this.get()
}

fun defaultAllPublicAbility(name: String, info: String): Ability.AbilityBuilder {
    return Ability.builder()
            .name(name)
            .info(info)
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
}


fun UnsereWgBot.ensureWgGroup(messageContext: MessageContext, action: () -> Unit) = ensureWgGroup(messageContext.chatId(), action)
fun UnsereWgBot.ensureWgGroup(message: Message, action: () -> Unit) = ensureWgGroup(message.chatId, action)

fun UnsereWgBot.ensureWgGroup(chatId: Long, action: () -> Unit) {
    if (!this.telegramBotConfig.allowedGroupIds.contains(chatId)) {
        silent.send("Hey, leider kann dieser WG-Bot nur in der WG-Gruppe benutzt werden.", chatId)
        return
    }

    action()
}


val AbilityBot.silent get() = run {
    val declaredField = this.javaClass.superclass.getDeclaredField("silent")
    declaredField.isAccessible = true
    declaredField.get(this) as SilentSender
}

