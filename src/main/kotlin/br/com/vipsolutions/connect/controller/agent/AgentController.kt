package br.com.vipsolutions.connect.controller.agent

import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.websocket.SessionCentral
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 10/11/21
 */
@RestController
@RequestMapping("/api/agents")
class AgentController(private val companyRepository: CompanyRepository) {

    @GetMapping("/{controlNumber}")
    fun listMemoryAgentSessions(@PathVariable controlNumber: Long) = companyRepository.findByControlNumber(controlNumber)
        .flatMap { Mono.justOrEmpty(SessionCentral.getAllByCompanyId(it.id)) }
        .map { it.keys.toList() }
        .switchIfEmpty(Mono.just(listOf()))

    @GetMapping
    fun listAllMemoryAgentSessions() = Mono.justOrEmpty(SessionCentral.getAll())
        .switchIfEmpty(Mono.just(listOf()))
}