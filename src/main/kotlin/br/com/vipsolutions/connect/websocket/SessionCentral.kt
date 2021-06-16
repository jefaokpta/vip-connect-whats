package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

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

fun alertNewMessageToAgents(contact: Contact) = Optional.ofNullable(SessionCentral.agents[contact.company])
    .map { Flux.fromIterable(it.values) }
    .orElse(Flux.empty())
    .flatMap {it.send(Mono.just(it.textMessage(objectToJson(AgentActionWs("NEW_MESSAGE", 0, 0, null, contact, null, null))))) }
    .subscribe() // preciso dar um jeito de apagar o agente qnd desconectar do ws

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