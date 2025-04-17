package com.youchunmaru

import com.youchunmaru.controller.databases.configureDatabases
import com.youchunmaru.controller.router.configureRouting
import com.youchunmaru.controller.security.configureSecurity
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureDatabases()
    configureRouting()
}
