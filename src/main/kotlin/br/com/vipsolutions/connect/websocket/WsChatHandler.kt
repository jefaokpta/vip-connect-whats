package br.com.vipsolutions.connect.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Service
class WsChatHandler : WebSocketHandler {

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .doFirst {
            println("SESSION ID : ${session.id} CONECTOU ${SessionCentral.sessions.size}")
        }
        .map { addSession(it, session) }
        .map(session::textMessage))
        .doFinally {
            SessionCentral.sessions.values.remove(session)
            println("SESSION FOI EMBORA ID: " + session.id)
        }

    fun addSession(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): String {
        ObjectMapper().readValue(webSocketMessage.payloadAsText, ObjectNode::class.java).let {
            //SessionCentral.sessions[it.get("data").get("agent").intValue()] = webSocketSession
            println(it)
        }
        return webSocketMessage.payloadAsText
    }


}