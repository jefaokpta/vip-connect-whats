package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.sendQuizButtonsMessage
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.repository.*
import br.com.vipsolutions.connect.service.WsChatHandlerService
import br.com.vipsolutions.connect.util.*
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
    private val whatsChatRepository: WhatsChatRepository,
    private val wsChatHandlerService: WsChatHandlerService,
) : WebSocketHandler {

    private val missingContactErrorMessage = "FALTA OBJ CONTATO"

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleAgentActions(it, session) }
        .doFinally { removeAgentSession(session)}
    )

    private fun handleAgentActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage>{
        val agentActionWs = Gson().fromJson(webSocketMessage.payloadAsText, AgentActionWs::class.java)

        return when(agentActionWs.action){
            "ONLINE" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .map { addAgentSession(it, agentActionWs, webSocketSession)  }
                .map { company -> contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(company.id) }
                .flatMap { it.collectList() }
                .map { contacts -> contacts.filter { agentActionWs.categories.contains(it.category) } }
                .map { contacts -> contacts.filter { !it.fromAgent }}
                .map (::contactsHaveNewMessages)
                .map { verifyLockedContacts(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }
//                .log()

            "UPDATE_CONTACT" -> Mono.justOrEmpty(agentActionWs.contact)
                .flatMap { contactRepository.save(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(AgentActionWs(agentActionWs.action,
                    agentActionWs.agent, agentActionWs.controlNumber, null, null, null, null, null)))))

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
                    if(agentActionWs.contact!!.isNewProtocol){
                        agentActionWs.contact!!.isNewProtocol = false
                        return contactRepository.save(agentActionWs.contact!!)
                            .map { webSocketSession.textMessage(objectToJson(agentActionWs)) }
                            .doFinally { unlockContact(agentActionWs.contact!!, agentActionWs.agent).subscribe() }
                    }
                    return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
                        .doFinally { unlockContact(agentActionWs.contact!!, agentActionWs.agent).subscribe() }
                }
                agentActionWs.action = "ERROR"
                agentActionWs.errorMessage = "FALTANDO CONTATO OU AGENTE"
                return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
            }

            "PROFILE_PICTURE" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = missingContactErrorMessage })))
                val profilePicture = getProfilePicture(contact.instanceId, contact.whatsapp).picture?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "NAO FOI POSSSIVEL OBTER FOTO DO PERFIL" })))
                contact.imgUrl = profilePicture
                return contactRepository.save(contact)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
            }

            "FINALIZE_ATTENDANCE" -> {
                val contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = missingContactErrorMessage })))
                contact.category = null
                contact.protocol = null
                contact.isNewProtocol = false
                return contactRepository.save(contact)
                    .flatMap (wsChatHandlerService::sendQuizOrFinalizeMsg)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { action = "FINALIZE_ATTENDANCE_RESPONSE" })) }
                    .doOnNext { broadcastToAgents(contact, "FINALIZE_ATTENDANCE").subscribe() }
            }

            "LIST_CONTACTS_LAST_CATEGORY" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .flatMap{ wsChatHandlerService.contactsFilteredByLastCategory(it, agentActionWs.agent) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "LIST_ALL_CONTACTS" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .map { contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(it.id) }
                .flatMap { it.collectList() }
                .map (::contactsHaveNewMessages)
                .map { verifyLockedContacts(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "ACTIVE_CHAT" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = missingContactErrorMessage })))
                contact.lastCategory = contact.category?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "FALTANDO DEFINIR CATEGORIA!" })))
                contact.fromAgent = true
                contactRepository.save(generateProtocol(contact))
                    .doOnNext { AnsweringUraCenter.contacts.remove(it.whatsapp) }
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                    .doFinally {
                        unlockContact(contact, agentActionWs.agent).subscribe()
                        lockContact(contact, agentActionWs.agent).subscribe()
                    }
            }

            "TRANSFER_CONTACT" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = missingContactErrorMessage })))
                contact.lastCategory = contact.category?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "FALTANDO DEFINIR CATEGORIA!" })))
                contact.isNewProtocol = false
                contactRepository.save(contact)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                    .doFinally {
                        unlockContact(contact, agentActionWs.agent).subscribe()
                        alertNewMessageToAgents(contact).subscribe()
                        wsChatHandlerService.sendTransferMessage(contact).subscribe()
                    }
            }

            else -> Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply {action = "ERROR"; errorMessage = "Açao Desconhecida." })))
        }
    }

}