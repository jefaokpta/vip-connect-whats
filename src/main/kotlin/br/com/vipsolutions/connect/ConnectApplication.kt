package br.com.vipsolutions.connect

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ConnectApplication

fun main(args: Array<String>) {
    runApplication<ConnectApplication>(*args)
}
