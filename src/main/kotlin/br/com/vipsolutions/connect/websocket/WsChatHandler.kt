package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.sendTextMessage
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
        //println(webSocketMessage.payloadAsText)
        val agentActionWs = Gson().fromJson(webSocketMessage.payloadAsText, AgentActionWs::class.java)

        return when(agentActionWs.action){
            "ONLINE" -> companyRepository.findByCompany(agentActionWs.company)
                .map { addAgentSession(it, agentActionWs, webSocketSession)  }
                .map { contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(it.id) }
                .flatMap { it.collectList() }
                .map (::contactsHaveNewMessages)
                .map { verifyLockedContacts(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "UPDATE_CONTACT" -> Mono.justOrEmpty(agentActionWs.contact)
                .flatMap { contactRepository.save(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(AgentActionWs(agentActionWs.action, agentActionWs.agent, agentActionWs.company, null, null, null, null, null)))))

            "CONTACT_ANSWERED" -> Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
                .doFinally {
                    Optional.ofNullable(agentActionWs.contact)
                        .map {
                            ContactCenter.contacts[it.company]?.remove(it.id)
                            clearNewMessageToAgents(it).subscribe()
                            unlockContact(it, agentActionWs.agent).subscribe()
                            lockContact(it, agentActionWs.agent).subscribe()
                        }
                }

            "CONTACT_MESSAGES" -> Optional.ofNullable(agentActionWs.contact)
                .map { whatsChatRepository.findTop50ByRemoteJidOrderByDatetimeDesc(it.whatsapp) }
                .orElse(Flux.empty())
                .collectList()
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { messages = it })) }

            "SEND_TXT_MESSAGE" -> {
                if (agentActionWs.message !== null && agentActionWs.contact !== null){
                    sendTextMessage(agentActionWs.message!!, agentActionWs.contact!!)
                }
                else{
                    agentActionWs.errorMessage = "FALTANDO OBJETO MESSAGE OU CONTACT"
                    agentActionWs.action = "ERROR"
                }
                return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
            }

            "UNLOCK_CONTACT" -> {
                if (agentActionWs.contact !== null && agentActionWs.agent > 0){
                    agentActionWs.action = "UNLOCK_CONTACT_RESPONSE"
                    return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
                        .doFinally { unlockContact(agentActionWs.contact!!, agentActionWs.agent).subscribe() }
                }
                agentActionWs.action = "ERROR"
                agentActionWs.errorMessage = "FALTANDO CONTATO OU AGENTE"
                return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
            }

            "PROFILE_PICTURE" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "FALTA OBJ CONTATO" })))
                val profilePicture = getProfilePicture(contact.instanceId, contact.whatsapp).picture?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "NAO FOI POSSSIVEL OBTER FOTO DO PERFIL" })))
                contact.imgUrl = profilePicture
                return contactRepository.save(contact)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
            }
            else -> Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply {action = "ERROR"; errorMessage = "Açao Desconhecida." })))
        }
    }

}