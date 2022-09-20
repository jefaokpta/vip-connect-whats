package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.CompanyInfo
import br.com.vipsolutions.connect.model.ws.ActionWs
import br.com.vipsolutions.connect.model.ws.QrCode
import br.com.vipsolutions.connect.repository.AuthWhatsappRepository
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.util.RegisterCompanyCenter
import br.com.vipsolutions.connect.util.destroyInstance
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-23
 */
@RestController
@RequestMapping("/api/register")
class RegisterCell(private val authWhatsappRepository: AuthWhatsappRepository, private val companyRepository: CompanyRepository) {

    @GetMapping("/status/{controlNumber}")
    fun companyStatus(@PathVariable controlNumber: Long) = companyRepository.findById(controlNumber)
        .map { CompanyInfo(it, "") }
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada")))

    @PostMapping
    fun receiveQrCode(@RequestBody qrCode: QrCode): Mono<Void> {
        println("QRCODE RECEBIDO: $qrCode")
        verifyRegisterTries(qrCode.id)
        return Mono.justOrEmpty(RegisterCompanyCenter.companies[qrCode.id])
            .flatMap { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("QRCODE", 0, qrCode.id, qrCode, null))))) }
            .then()
    }


    @PostMapping("/auth/{companyId}")
    fun confirmedAuthWhats(@PathVariable companyId: Long) = Mono.justOrEmpty(Optional.ofNullable(RegisterCompanyCenter.companies[companyId]))
        .map { it.send(Mono.just(it.textMessage(objectToJson(ActionWs("REGISTERED", 0, 0, null, null))))) }
        .doFinally { println("CONFIRMADO CONEXAO WHATS $companyId"); RegisterCompanyCenter.removeTries(companyId) }

    @GetMapping("/auth/{companyId}")
    fun restoreAuthWhats(@PathVariable companyId: Long) = authWhatsappRepository.findByCompanyId(companyId)

    private fun verifyRegisterTries(companyId: Long){
        val tries = RegisterCompanyCenter.plusRegisterTries(companyId)
        println("TENTATIVAS DE REGISTRO: $tries")
        if(tries > 30){
            RegisterCompanyCenter.removeTries(companyId)
            companyRepository.findById(companyId)
                .flatMap { companyRepository.save(it.copy(isActive = false)) }
                .map(::destroyInstance)
                .subscribe()
        }
    }
}