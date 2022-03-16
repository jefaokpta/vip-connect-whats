package br.com.vipsolutions.connect.controller

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

    @PostMapping("/auth/{companyId}")
    fun confirmedAuthWhats(@PathVariable companyId: Long) = Mono.justOrEmpty(Optional.ofNullable(RegisterCompanyCenter.companies[companyId]))
        .map { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("REGISTERED", 0, 0, null, null))))) }
        .doFinally { println("CONFIRMADO CONEXAO WHATS $companyId") }

    @GetMapping("/auth/{companyId}")
    fun restoreAuthWhats(@PathVariable companyId: Long) = authWhatsappRepository.findByCompanyId(companyId)

}