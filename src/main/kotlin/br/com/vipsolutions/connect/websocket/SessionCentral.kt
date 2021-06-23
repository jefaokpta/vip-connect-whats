package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.model.ws.AgentSession
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import kotlin.collections.HashMap

class SessionCentral {

    companion object {
        val agents = mutableMapOf<Long, MutableMap<Int, AgentSession>>()
    }
}

fun unlockContact(contact: Contact, agent: Int): Flux<Void> {
    val agentSession = SessionCentral.agents[contact.company]?.get(agent) ?: return Flux.empty()
    println("APAGANDO CONTATO PRIMEIRO")
    if (agentSession.contact !== null){
        val contactCopy = Contact(agentSession.contact!!)
        contactCopy.locked = false
        agentSession.contact = null
        return broadcastToAgents(contactCopy, "UNLOCK_CONTACT")
    }
    return Flux.empty()
}

fun lockContact(contact: Contact, agent: Int): Flux<Void> {
    val agentSession = SessionCentral.agents[contact.company]?.get(agent) ?: return Flux.empty()
    agentSession.contact = contact
    contact.locked = true
    return broadcastToAgents(contact, "LOCK_CONTACT")
}

fun clearNewMessageToAgents(contact: Contact): Flux<Void> {
    return broadcastToAgents(contact.apply { newMessage = false; newMessageQtde = 0 }, "CONTACT_READED")
}

private fun broadcastToAgents(contact: Contact, action: String) = Optional.ofNullable(SessionCentral.agents[contact.company])
    .map { Flux.fromIterable(it.values) }
    .orElse(Flux.empty())
    .flatMap {it.session.send(Mono.just(it.session.textMessage(objectToJson(AgentActionWs(
        action,
        0,
        0,
        null,
        contact,
        null,
        null
    ))))) }

fun addAgentSession(company: Company, actionWs: AgentActionWs, webSocketSession: WebSocketSession): Company {
    if (SessionCentral.agents.contains(company.id)){
        SessionCentral.agents[company.id]!![actionWs.agent] = AgentSession(webSocketSession, null)
    }
    else{
        SessionCentral.agents[company.id] = mutableMapOf(actionWs.agent to AgentSession(webSocketSession, null))
    }
    return company
}

fun removeAgentSession(session: WebSocketSession){
    val agentsMap = HashMap(SessionCentral.agents)
    agentsMap.forEach{companyMap ->
        companyMap.value.forEach{sessionMap ->
            if (session == sessionMap.value.session){
                //companyMap.value.remove(sessionMap.key)
                SessionCentral.agents[companyMap.key]?.remove(sessionMap.key)
                println("removido agente chave ${sessionMap.key} do mapa id ${companyMap.key}")
            }
        }
    }
    agentsMap.clear()
}

fun alertNewMessageToAgents(contact: Contact) = Optional.ofNullable(SessionCentral.agents[contact.company])
    .map { Flux.fromIterable(it.values) }
    .orElse(Flux.empty())
    .flatMap {it.session.send(Mono.just(it.session.textMessage(objectToJson(AgentActionWs("NEW_MESSAGE", 0, 0, null, contact, null, null))))) }
    .subscribe()

