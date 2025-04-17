package com.youchunmaru.model.service

import com.youchunmaru.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserService (database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50).uniqueIndex()
        val password = varchar("password", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    private suspend fun create(user: User): Int = dbQuery {
        Users.insert {
            it[name] = user.name
            it[password] = user.password
        }[Users.id]
    }

    suspend fun read(name: String): User? {
        return dbQuery {
            Users.selectAll()
                .where { Users.name eq name }
                .map { User(it[Users.name], it[Users.password]) }
                .singleOrNull()
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}