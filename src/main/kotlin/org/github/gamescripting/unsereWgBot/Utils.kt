package org.github.gamescripting.unsereWgBot

import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.api.objects.Update
import java.util.*

fun <T> Optional<T>.asNullable(): T? {
    if(!this.isPresent) {
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
