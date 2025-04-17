package com.youchunmaru.controller.security

import com.youchunmaru.controller.databases.DatabaseController
import com.youchunmaru.controller.security.SecurityController.Companion.checkUser
import com.youchunmaru.model.User
import com.youchunmaru.util.PasswordEncoder
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

class SecurityController {
    companion object {
        private val instance = SecurityController()
        fun getInstance(): SecurityController {
            return instance
        }
        suspend fun checkUser(credentials: UserPasswordCredential): UserIdPrincipal? {
            val user = DatabaseController.getInstance().getUserService().read(credentials.name)
            return if (user != null) {
                if (PasswordEncoder().matches(credentials.password, user.password)) {
                    UserIdPrincipal(credentials.name)
                }else{
                    null
                }
            }else{
                null
            }
        }
    }
    private constructor()
}

fun Application.configureSecurity() {
    authentication {
        basic(name = "basic") {
            realm = "Ktor Server"
            validate { credentials ->
               checkUser(credentials)
            }
        }
        session<MySession>("auth-session") {
            validate { session ->
                if (session.username.isNotEmpty()){
                    val user = DatabaseController.getInstance().getUserService().read(session.username)
                    if (user != null){
                        session
                    }else{
                        null
                    }
                }else{
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }

        form(name = "form") {
            userParamName = "user"
            passwordParamName = "password"
            validate { credentials ->
                checkUser(credentials)
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    routing {
        authenticate ("auth-session") {  }//use this everywhere
        authenticate("basic") {
        }

        get("login"){
            call.respond(ThymeleafContent("login.html", mapOf("user" to User("",""))))
        }
        authenticate("form") {
            post("/login") {
                val userName = call.principal<UserIdPrincipal>()?.name.toString()
                call.sessions.set(MySession(userName))
                call.respondRedirect("/")
            }
        }
    }
}

@Serializable
data class MySession(val username: String)