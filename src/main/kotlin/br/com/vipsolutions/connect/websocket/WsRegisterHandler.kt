package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.CompanyInfo
import br.com.vipsolutions.connect.model.ws.ActionWs
import br.com.vipsolutions.connect.service.CompanyService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Service
class WsRegisterHandler(private val companyService: CompanyService) : WebSocketHandler {

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleActions(it, session) }
        )

    private fun handleActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage> {
        val action = Gson().fromJson(webSocketMessage.payloadAsText, ActionWs::class.java)
        return when (action.action) {
            "REGISTER" -> companyService.createCompany(action.company)
                .map { webSocketSession.textMessage(jacksonObjectMapper().writeValueAsString(it)) }
            else -> Mono.just(webSocketSession.textMessage(Gson().toJson(CompanyInfo(0, 0, 0, "ACAO INVALIDA!"))))
        }
    }


}