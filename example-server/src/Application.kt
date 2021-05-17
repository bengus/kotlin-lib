package ru.bengus.exampleserver

import io.ktor.application.*
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import ru.bengus.kotlinlib.TestKotlinClass
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val env = applicationEngineEnvironment {
        module { module() }
        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }
    val server = embeddedServer(Netty, env).start(false)

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1L, 5L, TimeUnit.SECONDS)
    })
    Thread.currentThread().join()
}

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    environment.monitor.subscribe(ApplicationStarted) {
        println("My app 1 is ready to roll")
    }
    environment.monitor.subscribe(ApplicationStopped) {
        println("Time to clean up 1")
    }

    routing {
        get("/") {
            log.trace("request to api 1")
            val aa = TestKotlinClass()
            call.respondText("Connected to public api 1 ${aa.a} ${aa.aa}")
        }
    }
}
