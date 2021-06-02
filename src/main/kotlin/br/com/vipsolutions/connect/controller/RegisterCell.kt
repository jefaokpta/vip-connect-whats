package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.ws.ActionWs
import br.com.vipsolutions.connect.model.ws.QrCode
import br.com.vipsolutions.connect.util.RegisterCompanyCenter
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-23
 */
@RestController
@RequestMapping("/api/register")
class RegisterCell {

    @PostMapping
    fun receiveQrCode(@RequestBody qrCode: QrCode) = Mono.justOrEmpty(RegisterCompanyCenter.companies[qrCode.id])
        .flatMap { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("QRCODE", 0, qrCode.id, qrCode, null))))) }

    @GetMapping("/{id}")
    fun confirmedQrCode(@PathVariable id: Long) = Mono.justOrEmpty(RegisterCompanyCenter.companies[id])
        .flatMap { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("REGISTERED", 0, id, null, null))))) }
        .doFirst { println("CONFIRMADO ID $id") }


}