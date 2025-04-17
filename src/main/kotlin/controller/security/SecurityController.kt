package com.youchunmaru.controller.security

import com.youchunmaru.model.User
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

fun Application.configureSecurity() {
    authentication {
        basic(name = "myauth1") {
            realm = "Ktor Server"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        session<MySession>("auth-session") {
            validate { session ->
                if (session.count > 0){
                    session
                }else{
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }

        form(name = "myauth2") {
            userParamName = "user"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
                /**/
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
        authenticate("myauth1") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        get("login"){
            call.respond(ThymeleafContent("login.html", mapOf("user" to User("",""))))
        }
        authenticate("myauth2") {
            post("/login") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
                val userName = call.principal<UserIdPrincipal>()?.name.toString()
                call.sessions.set(MySession(count = 1))
                call.respondRedirect("/hello")
            }
            get("/protected/route/form") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}

@Serializable
data class MySession(val count: Int = 0)