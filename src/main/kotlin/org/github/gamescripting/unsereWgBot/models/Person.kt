package org.github.gamescripting.unsereWgBot.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
class Person {

    @Id
    var macAdress: String? = null

    var displayName: String? = null

}