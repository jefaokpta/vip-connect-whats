package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.AuthWhatsapp
import br.com.vipsolutions.connect.model.ws.ActionWs
import br.com.vipsolutions.connect.model.ws.QrCode
import br.com.vipsolutions.connect.repository.AuthWhatsappRepository
import br.com.vipsolutions.connect.util.RegisterCompanyCenter
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-23
 */
@RestController
@RequestMapping("/api/register")
class RegisterCell(private val authWhatsappRepository: AuthWhatsappRepository) {

    @PostMapping
    fun receiveQrCode(@RequestBody qrCode: QrCode) = Mono.justOrEmpty(RegisterCompanyCenter.companies[qrCode.id])
        .flatMap { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("QRCODE", 0, qrCode.id, qrCode, null))))) }
        .doFirst { println("QRCODE RECEBIDO $qrCode") }

    @PostMapping("/auth")
    fun confirmedAuthWhats(@RequestBody authWhatsapp: AuthWhatsapp) = authWhatsappRepository.findByCompanyId(authWhatsapp.companyId)
        .flatMap { authWhatsappRepository.save(authWhatsapp.apply { id = it.id }) }
        .switchIfEmpty(authWhatsappRepository.save(authWhatsapp))
        .map { Optional.ofNullable(RegisterCompanyCenter.companies[it.companyId]) }
        .flatMap { oWss ->
            oWss.map { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("REGISTERED", 0, 0, null, null))))) }
                .orElse(Mono.empty())
        }
        .doFinally { println("CONFIRMADO NOVA AUTH WHATS") }

    @GetMapping("/auth/{companyId}")
    fun restoreAuthWhats(@PathVariable companyId: Long) = authWhatsappRepository.findByCompanyId(companyId)

//    @GetMapping("/{id}")
//    fun confirmedQrCode(@PathVariable id: Long) = Mono.justOrEmpty(RegisterCompanyCenter.companies[id])
//        .flatMap { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("REGISTERED", 0, id, null, null))))) }
//        .doFirst { println("CONFIRMADO ID $id") }


}