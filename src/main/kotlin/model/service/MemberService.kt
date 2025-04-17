package com.youchunmaru.model.service

import com.youchunmaru.model.Member
import com.youchunmaru.model.MemberDetail
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class MemberService(database: Database) {
    object Members : Table() {
        val id = integer("id").autoIncrement()
        val firstName = varchar("firstName", length = 50)
        val lastName = varchar("lastName", length = 50)

        override val primaryKey = PrimaryKey(id)
    }
    object MemberDetails : Table() {
        val id = (integer("member_id") references Members.id)
        val email = varchar("email", length = 50).nullable()
        val address = varchar("address", 255).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Members)
            SchemaUtils.create(MemberDetails)
        }
    }

    suspend fun create(user: Member) : Int = dbQuery {
        createDetail(createUser(user), user.detail)
    }

    private suspend fun createDetail(memberId: Int, detail: MemberDetail?) : Int = dbQuery {
        MemberDetails.insert {
            it[id] = memberId
            it[email] = detail?.email
            it[address] = detail?.address
        }[MemberDetails.id]
    }

    private suspend fun createUser(member: Member): Int = dbQuery {
        Members.insert {
            it[firstName] = member.firstName
            it[lastName] = member.lastName
        }[Members.id]
    }

    suspend fun read(id: Int): Member? {
        return dbQuery {
            Members.selectAll()
                .where { Members.id eq id }
                .map { Member(it[Members.firstName], it[Members.lastName], null) }
                .singleOrNull()
        }
    }

    suspend fun readWithDetails(id: Int): Member?{
        return dbQuery {
            Members.selectAll()
                .where {Members.id eq id}
                .map {Member(it[Members.firstName], it[Members.lastName], readDetails(id))}
                .singleOrNull()
        }
    }

    private suspend fun readDetails(id: Int): MemberDetail?{
        return dbQuery {
            MemberDetails.selectAll()
                .where {MemberDetails.id eq id}
                .map {MemberDetail(it[MemberDetails.email], it[MemberDetails.address])}
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, member: Member) {
        dbQuery {
            Members.update({ Members.id eq id }) {
                it[firstName] = member.firstName
                it[lastName] = member.lastName
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Members.deleteWhere { Members.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}