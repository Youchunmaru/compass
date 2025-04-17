package com.youchunmaru.controller.router.user

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val basePath = "/user"

fun Application.userRouting() {
    routing {
        get(basePath) {
            call.respondText("Hello World!")
        }
    }
}