package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import kotlin.collections.HashMap

class SessionCentral {

    companion object {
        val agents = mutableMapOf<Long, MutableMap<Int, WebSocketSession>>()
    }
}

fun addAgentSession(company: Company, actionWs: AgentActionWs, webSocketSession: WebSocketSession): Company {
    if (SessionCentral.agents.contains(company.id)){
        SessionCentral.agents[company.id]!![actionWs.agent] = webSocketSession
    }
    else{
        SessionCentral.agents[company.id] = mutableMapOf(actionWs.agent to webSocketSession)
    }
    return company
}

fun removeAgentSession(session: WebSocketSession){
    val agentsMap = HashMap(SessionCentral.agents)
    agentsMap.forEach{companyMap ->
        companyMap.value.forEach{sessionMap ->
            if (session == sessionMap.value){
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
    .flatMap {it.send(Mono.just(it.textMessage(objectToJson(AgentActionWs("NEW_MESSAGE", 0, 0, null, contact, null, null))))) }
    .subscribe()

fun clearNewMessageToAgents(contact: Contact) = Optional.ofNullable(SessionCentral.agents[contact.company])
    .map { Flux.fromIterable(it.values) }
    .orElse(Flux.empty())
    .flatMap {it.send(Mono.just(it.textMessage(objectToJson(AgentActionWs(
        "CONTACT_READED",
        0,
        0,
        null,
        contact.apply { newMessage = false; newMessageQtde = 0 },
        null,
        null
    ))))) }
    .subscribe()