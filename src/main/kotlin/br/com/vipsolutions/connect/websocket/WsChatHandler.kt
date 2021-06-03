package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.util.objectToJson
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Service
class WsChatHandler : WebSocketHandler {

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleAgentActions(it, session) }
    )

    private fun handleAgentActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage>{
        return Mono.just(webSocketSession.textMessage(objectToJson("teste")))
    }


}