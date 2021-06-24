package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import br.com.vipsolutions.connect.util.ContactCenter
import br.com.vipsolutions.connect.util.contactsHaveNewMessages
import br.com.vipsolutions.connect.util.objectToJson
import com.google.gson.Gson
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class WsChatHandler(
    private val contactRepository: ContactRepository,
    private val companyRepository: CompanyRepository,
    private val whatsChatRepository: WhatsChatRepository
) : WebSocketHandler {

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleAgentActions(it, session) }
        .doFinally { removeAgentSession(session)}
    )

    private fun handleAgentActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage>{
        val agentActionWs = Gson().fromJson(webSocketMessage.payloadAsText, AgentActionWs::class.java)

        return when(agentActionWs.action){
            "ONLINE" -> companyRepository.findByCompany(agentActionWs.company)
                .map { addAgentSession(it, agentActionWs, webSocketSession)  }
                .map { contactRepository.findAllByCompany(it.id) }
                .map (::contactsHaveNewMessages)
                .flatMap { it.collectList() }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "UPDATE_CONTACT" -> Mono.justOrEmpty(agentActionWs.contact)
                .flatMap { contactRepository.save(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(AgentActionWs(agentActionWs.action, agentActionWs.agent, agentActionWs.company, null, null, null, null, null)))))

            "CONTACT_MESSAGES" -> Optional.ofNullable(agentActionWs.contact)
                .map { whatsChatRepository.findTop50ByRemoteJidOrderByDatetimeDesc(it.whatsapp) }
                .orElse(Flux.empty())
                .collectList()
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { messages = it })) }
                .doFinally {
                    Optional.ofNullable(agentActionWs.contact)
                        .map {
                            ContactCenter.contacts[it.company]?.remove(it.id)
                            clearNewMessageToAgents(it).subscribe()
                            unlockContact(it, agentActionWs.agent).subscribe()
                            lockContact(it, agentActionWs.agent).subscribe()
                        }
                }

            else -> Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "Açao Desconhecida." })))
        }
    }

}