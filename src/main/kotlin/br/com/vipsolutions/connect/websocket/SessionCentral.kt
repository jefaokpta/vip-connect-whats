package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ContactsAndId
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.model.ws.AgentSession
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

class SessionCentral {

    companion object {
        val agents = mutableMapOf<Long, MutableMap<Int, AgentSession>>()
    }
}

fun verifyLockedContacts(contactsAndId: ContactsAndId): List<Contact> {
    val contactsBusy = mutableMapOf<Long, Int>()
    SessionCentral.agents[contactsAndId.companyId]?.values?.forEach {
        if (it.contact !== null){
            contactsBusy[it.contact!!.id] = 0
        }
    } ?: return contactsAndId.contacts

    contactsAndId.contacts.forEach{ contact ->
        if (contactsBusy.containsKey(contact.id)) {
            contact.busy = true
        }
    }
    return contactsAndId.contacts
}

fun unlockContact(contact: Contact, agent: Int): Flux<Void> {
    val agentSession = SessionCentral.agents[contact.company]?.get(agent) ?: return Flux.empty()
    if (agentSession.contact !== null){
        val contactCopy = Contact(agentSession.contact!!)
        contactCopy.busy = false
        agentSession.contact = null
        return broadcastToAgents(contactCopy, "UNLOCK_CONTACT")
    }
    return Flux.empty()
}

fun lockContact(contact: Contact, agent: Int): Flux<Void> {
    val agentSession = SessionCentral.agents[contact.company]?.get(agent) ?: return Flux.empty()
    agentSession.contact = contact
    contact.busy = true
    return broadcastToAgents(contact, "LOCK_CONTACT")
}

fun clearNewMessageToAgents(contact: Contact): Flux<Void> {
    return broadcastToAgents(contact.apply { newMessage = false; newMessageQtde = 0 }, "CONTACT_READED")
}

fun broadcastToAgents(contact: Contact, action: String) = Optional.ofNullable(SessionCentral.agents[contact.company])
    .map { Flux.fromIterable(it.values) }
    .orElse(Flux.empty())
    .flatMap {it.session.send(Mono.just(it.session.textMessage(objectToJson(AgentActionWs(
        action,
        0,
        0,
        null,
        contact,
        null,
        null,
        null
    ))))) }

fun addAgentSession(company: Company, actionWs: AgentActionWs, webSocketSession: WebSocketSession): Company {
    //actionWs.category.forEach { println(it) }
    if (SessionCentral.agents.contains(company.id)){
        SessionCentral.agents[company.id]!![actionWs.agent] = AgentSession(webSocketSession, null, actionWs.categories)
    }
    else{
        SessionCentral.agents[company.id] = mutableMapOf(actionWs.agent to AgentSession(webSocketSession, null, actionWs.categories))
    }
    return company
}

fun removeAgentSession(session: WebSocketSession){
    val agentsMap = HashMap(SessionCentral.agents)
    agentsMap.forEach{companyMap ->
        companyMap.value.forEach{sessionMap ->
            if (session == sessionMap.value.session){
                SessionCentral.agents[companyMap.key]?.remove(sessionMap.key)
                println("removido agente chave ${sessionMap.key} do mapa id ${companyMap.key} - ${LocalDateTime.now()}")
                return
            }
        }
    }
}

fun contactOnAttendance(contact: Contact, whatsChat: WhatsChat): Contact {
    val agents = SessionCentral.agents[contact.company]?: return contact
    agents.forEach { agent ->
        if (agent.value.contact !== null){
            if (agent.value.contact!!.id == contact.id){
                contact.busy = true
                agent.value.session.send(Mono.just(agent.value.session.textMessage(objectToJson(AgentActionWs("MESSAGE_IN_ATTENDANCE", 0, 0, null, contact, null, whatsChat, null)))))
                    .subscribe()
            }
        }
    }
    return contact
}

fun alertNewMessageToAgents(contact: Contact): Flux<Void> {
    if (!contact.busy){
        return Optional.ofNullable(SessionCentral.agents[contact.company])
            .map { agentSession ->
                Flux.fromIterable(agentSession.values).filter { it.categories.contains(contact.category) }
            }
            .orElse(Flux.empty())
            .flatMap {it.session.send(Mono.just(it.session.textMessage(objectToJson(AgentActionWs("NEW_MESSAGE", 0, 0, null, contact, null, null, null))))) }

    }
    return Flux.empty()
}

