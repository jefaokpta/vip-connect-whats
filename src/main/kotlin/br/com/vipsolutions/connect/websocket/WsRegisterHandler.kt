package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.CompanyInfo
import br.com.vipsolutions.connect.model.ws.ActionWs
import br.com.vipsolutions.connect.repository.AuthWhatsappRepository
import br.com.vipsolutions.connect.service.CompanyService
import br.com.vipsolutions.connect.util.RegisterCompanyCenter
import br.com.vipsolutions.connect.util.objectToJson
import com.google.gson.Gson
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Service
class WsRegisterHandler(
    private val companyService: CompanyService,
    private val authWhatsappRepository: AuthWhatsappRepository
) : WebSocketHandler {

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleActions(it, session) }
        )

    private fun handleActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage> {
        val action = Gson().fromJson(webSocketMessage.payloadAsText, ActionWs::class.java)
        return when (action.action) {
            "INFO" -> companyService.infoCompany(action.controlNumber)
                .map { webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(it, "")))) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(0, 0, 0, "Empresa invalida"))))))

            "REGISTER" -> companyService.createCompany(action.controlNumber)
                .map { RegisterCompanyCenter.companies[it.id] = webSocketSession; it }
                .map { webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, it))) }

            "STOP" -> companyService.stopCompany(action.instanceId)
                .map { webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, it))) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(0, 0, 0, "Empresa invalida"))))))

            "START" -> companyService.startCompany(action.instanceId)
                .map { webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, it))) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(0, 0, 0, "Empresa invalida"))))))

            "DESTROY" -> companyService.destroyCompany(action.instanceId)
                .map { webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, it))) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(0, 0, 0, "Empresa invalida"))))))
                //.doFinally { authWhatsappRepository.deleteByCompanyId(action.instanceId).subscribe() }

            "DESTROY_AUTH" -> companyService.destroyCompany(action.instanceId)
                .map { webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, it))) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(0, 0, 0, "Empresa invalida"))))))
                .doFinally { authWhatsappRepository.deleteByCompanyId(action.instanceId).subscribe() }

            else -> Mono.just(webSocketSession.textMessage(objectToJson(ActionWs(action.action, action.controlNumber, action.instanceId, null, CompanyInfo(0, 0, 0, "Empresa invalida")))))
        }
    }

}