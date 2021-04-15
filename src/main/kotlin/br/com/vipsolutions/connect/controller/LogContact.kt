package br.com.vipsolutions.connect.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-12
 */
@RestController
@RequestMapping("/api/logs/contact")
class LogContact {

    @GetMapping("/{id}")
    fun fake(@PathVariable id: String) = Mono.just("{}")
}