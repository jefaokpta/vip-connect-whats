package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.ContactLite
import br.com.vipsolutions.connect.model.Group
import br.com.vipsolutions.connect.model.dao.GroupDAO
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import br.com.vipsolutions.connect.service.GroupService
import br.com.vipsolutions.connect.service.WsChatHandlerService
import br.com.vipsolutions.connect.util.*
import com.google.gson.Gson
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

@Service
class WsChatHandler(
    private val contactRepository: ContactRepository,
    private val companyRepository: CompanyRepository,
    private val whatsChatRepository: WhatsChatRepository,
    private val wsChatHandlerService: WsChatHandlerService,
    private val groupService: GroupService
) : WebSocketHandler {

    private final val MISSING_CONTACT_ERROR_MESSAGE = "FALTA OBJ CONTATO"
    private final val MISSING_GROUP_OBJECT = "FALTA OBJETO GROUP"
    private final val GROUP_NOT_FOUND = "GRUPO NÃO ENCONTRADO"

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleAgentActions(it, session) }
        .doFinally { SessionCentral.removeAgentSession(session)}
    )

    private fun handleAgentActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage>{
        val agentActionWs = Gson().fromJson(webSocketMessage.payloadAsText, AgentActionWs::class.java)

        return when(agentActionWs.action){
            "ONLINE" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .map { SessionCentral.addAgentSession(it, agentActionWs, webSocketSession)  }
                .map { company -> contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(company.id) }
                .flatMap { it.collectList() }
                .map { contacts -> contacts.filter { agentActionWs.categories.contains(it.category) } }
//                .map { contacts -> contacts.filter { !it.fromAgent }} // DESATIVADO PRA VER ONDE QUEBRA
                .map (ContactCenter::contactsHaveNewMessages)
                .map { SessionCentral.verifyLockedContacts(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "UPDATE_CONTACT" -> Mono.justOrEmpty(agentActionWs.contact)
                .flatMap { contactRepository.save(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(AgentActionWs(
                    agentActionWs.action,
                    agentActionWs.agent,
                    agentActionWs.controlNumber,
                )))))

            "CONTACT_ANSWERED" -> Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
                .doFinally {
                    Optional.ofNullable(agentActionWs.contact)
                        .map {
                            ContactCenter.remove(it.company, it.id)
                            SessionCentral.clearNewMessageToAgents(it).subscribe()
                            SessionCentral.unlockContact(it, agentActionWs.agent).subscribe()
                            SessionCentral.lockContact(it, agentActionWs.agent).subscribe()
                        }
                }

            "CONTACT_MESSAGES" -> Optional.ofNullable(agentActionWs.contact)
                .map { whatsChatRepository.findTop500ByRemoteJidAndCompanyOrderByDatetimeDesc(it.whatsapp, it.company) }
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
                            .publishOn(Schedulers.boundedElastic())
                            .doFinally { SessionCentral.unlockContact(agentActionWs.contact!!, agentActionWs.agent).subscribe() }
                    }
                    return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
                        .publishOn(Schedulers.boundedElastic())
                        .doFinally { SessionCentral.unlockContact(agentActionWs.contact!!, agentActionWs.agent).subscribe() }
                }
                agentActionWs.action = "ERROR"
                agentActionWs.errorMessage = "FALTANDO CONTATO OU AGENTE"
                return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs)))
            }

            "PROFILE_PICTURE" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_CONTACT_ERROR_MESSAGE })))
                val profilePicture = getProfilePicture(contact.instanceId, contact.whatsapp).picture?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "NAO FOI POSSSIVEL OBTER FOTO DO PERFIL" })))
                contact.imgUrl = profilePicture
                return contactRepository.save(contact)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
            }

            "FINALIZE_ATTENDANCE" -> {
                val contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_CONTACT_ERROR_MESSAGE })))
                wsChatHandlerService.sendQuizOrFinalizeMsg(contact)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { action = "FINALIZE_ATTENDANCE_RESPONSE" })) }
                    .publishOn(Schedulers.boundedElastic())
                    .doFinally { SessionCentral.broadcastToAgents(contact, "FINALIZE_ATTENDANCE").subscribe() }
            }

            "LIST_CONTACTS_LAST_CATEGORY" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .flatMap{ wsChatHandlerService.contactsFilteredByLastCategory(it, agentActionWs.agent) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "LIST_ALL_CONTACTS" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .map { contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(it.id) }
                .flatMap { it.collectList() }
                .map(ContactCenter::contactsHaveNewMessages)
                .map { SessionCentral.verifyLockedContacts(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "LIST_ALL_CONTACTS_LITE" -> companyRepository.findByControlNumber(agentActionWs.controlNumber)
                .map { contactRepository.findAllByCompanyOrderByNameAsc(it.id) }
                .flatMap { it.collectList() }
                .map { it.map(::ContactLite) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contactsLite = it })) }

            "ACTIVE_CHAT" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_CONTACT_ERROR_MESSAGE })))
                contact.lastCategory = contact.category?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "FALTANDO DEFINIR CATEGORIA!" })))
                contact.fromAgent = true
                contactRepository.save(generateProtocol(contact))
                    .doOnNext { AnsweringUraCenter.removeUraAnswer(it) }
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                    .publishOn(Schedulers.boundedElastic())
                    .doFinally {
                        SessionCentral.unlockContact(contact, agentActionWs.agent).subscribe()
                        SessionCentral.lockContact(contact, agentActionWs.agent).subscribe()
                    }
            }

            "TRANSFER_CONTACT" -> {
                var contact = agentActionWs.contact?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_CONTACT_ERROR_MESSAGE })))
                contact.lastCategory = contact.category?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "FALTANDO DEFINIR CATEGORIA!" })))
                contact.isNewProtocol = false
                contactRepository.save(contact)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                    .publishOn(Schedulers.boundedElastic())
                    .doFinally {
                        SessionCentral.unlockContact(contact, agentActionWs.agent).subscribe()
                        SessionCentral.alertNewMessageToAgents(contact).subscribe()
                        sendTextMessage(contact.whatsapp, "Você está no Departamento: ${agentActionWs.categoryName}", contact.instanceId)
                    }
            }
            "CREATE_GROUP" -> {
                val newGroup = agentActionWs.group?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_GROUP_OBJECT })))
                groupService.newGroup(newGroup)
//                    .switchIfEmpty(Mono.just(GroupDAO(0L, "", 0)))
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { group = it })) }
                    .onErrorResume { Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "NÃO FOI POSSIVEL CRIAR O GRUPO (ERROR)" }))) }
            }
            "UPDATE_GROUP" -> {
                val updateGroup = agentActionWs.group?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_GROUP_OBJECT })))
                groupService.updateGroup(updateGroup)
                    .switchIfEmpty (Mono.just(Group(0L, "", 0)))
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { if(it.id == 0L) errorMessage = "ERRO NO UPDATE DO GROUP - ELE EXISTE?" })) }
            }
            "DELETE_GROUP" -> {
                val deleteGroup = agentActionWs.group?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_GROUP_OBJECT })))
                groupService.deleteGroup(deleteGroup.id)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs)) }
                    .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs))))
            }
            "GROUP_WITH_CONTACT_LIST" -> {
                val groupReceived = agentActionWs.group?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = MISSING_GROUP_OBJECT })))
                groupService.getGroupWithContactList(groupReceived.id)
                    .switchIfEmpty(Mono.just(GroupDAO(0L, "", 0)))
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply {if(it.id == 0L) errorMessage = GROUP_NOT_FOUND else group = it})) }
            }
            "LIST_ALL_GROUPS" -> {
                groupService.getAllGroupsByControlNumber(agentActionWs.controlNumber)
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { groups = it.map(::GroupDAO) })) }
            }
            "SEND_GROUP_MESSAGE" -> {
                val groupMessage = agentActionWs.groupMessage?: return Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply { errorMessage = "FALTANDO GROUP MESSAGE" })))
                groupService.sendGroupMessage(groupMessage)
                    .switchIfEmpty(Mono.just(Group(0, "", 0)))
                    .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { if(it.id == 0L) errorMessage = GROUP_NOT_FOUND })) }
            }
            else -> Mono.just(webSocketSession.textMessage(objectToJson(agentActionWs.apply {action = "ERROR"; errorMessage = "Açao Desconhecida." })))
        }
    }

}