package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.util.loadVarsWeb
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 30/08/21
 */
@RestController
@RequestMapping("/api/envvars")
class ReloadEnvironmentVar {

    @GetMapping
    fun reload() = loadVarsWeb()
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "DEVE TER ERRO NO ARQUIVO env.json")))
}