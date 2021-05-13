package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.ws.qrCode
import br.com.vipsolutions.connect.util.RegisterCompanyCenter
import br.com.vipsolutions.connect.util.objectToJson
import com.google.gson.Gson
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
    fun receiveQrCode(@RequestBody qrCode: qrCode) = Mono.justOrEmpty(RegisterCompanyCenter.companies[qrCode.id])
        .flatMap { it.send(Mono.just(it.textMessage(objectToJson(qrCode)))) }

}