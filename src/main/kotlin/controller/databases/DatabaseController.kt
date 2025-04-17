package com.youchunmaru.controller.databases

import com.youchunmaru.model.Member
import com.youchunmaru.model.MemberDetail
import com.youchunmaru.model.service.MemberService
import com.youchunmaru.model.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.ThymeleafContent
import org.jetbrains.exposed.sql.*


class DatabaseController {

    private val database: Database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )

    private val memberService = MemberService(database)
    private val userService = UserService(database)

    companion object {
        private val instance = DatabaseController()
        fun getInstance(): DatabaseController {
            return DatabaseController.instance
        }
    }
    private constructor()

    fun getUserService() = userService
    fun getMemberService() = memberService
}


fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    val memberService = MemberService(database)

    routing {
        // Create user
        post("/users") {
            val user = call.receive<Member>()
            call.receiveText()
            val id = memberService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/users/c") {
            val id = memberService.create(Member("John", "Smith", MemberDetail("email", "address")))
            call.respond(HttpStatusCode.OK, id)
        }

        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = memberService.readWithDetails(id)
            if (user != null) {
                call.respond(ThymeleafContent("user/user", mapOf("user" to user)))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<Member>()
            memberService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }

        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            memberService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
