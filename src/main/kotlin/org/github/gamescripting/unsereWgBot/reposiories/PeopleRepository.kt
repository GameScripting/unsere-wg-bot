package org.github.gamescripting.unsereWgBot.reposiories

import org.github.gamescripting.unsereWgBot.models.Person
import org.springframework.data.jpa.repository.JpaRepository

interface PeopleRepository : JpaRepository<Person, Int> {
    fun findByMacAdress(macAdress: String): Person?
}