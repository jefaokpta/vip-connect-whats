package br.com.vipsolutions.connect.websocket

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ws.ActionWs
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.util.ContactCenter
import br.com.vipsolutions.connect.util.objectToJson
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
    private val companyRepository: CompanyRepository
) : WebSocketHandler {

    override fun handle(session: WebSocketSession) = session.send(session.receive()
        .flatMap { handleAgentActions(it, session) }
    )

    private fun handleAgentActions(webSocketMessage: WebSocketMessage, webSocketSession: WebSocketSession): Mono<WebSocketMessage>{
        val agentActionWs = Gson().fromJson(webSocketMessage.payloadAsText, AgentActionWs::class.java)

        return when(agentActionWs.action){
            "ONLINE" -> companyRepository.findByCompany(agentActionWs.company)
                .map { addAgentSession(it, agentActionWs, webSocketSession)  }
                .map { contactRepository.findAllByCompany(it.id) }
                .map (this::contactsHaveNewMessages)
                .flatMap { it.collectList() }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contacts = it })) }

            "UPDATE_CONTACT" -> Mono.justOrEmpty(agentActionWs.contact)
                .flatMap { contactRepository.save(it) }
                .map { webSocketSession.textMessage(objectToJson(agentActionWs.apply { contact = it })) }
                .switchIfEmpty(Mono.just(webSocketSession.textMessage(objectToJson(AgentActionWs(agentActionWs.action, agentActionWs.agent, agentActionWs.company, null, null)))))

            else -> Mono.just(webSocketSession.textMessage(objectToJson("teste")))
        }
    }

    private fun addAgentSession(company: Company, actionWs: AgentActionWs, webSocketSession: WebSocketSession): Company {
        if (SessionCentral.agents.contains(company.id)){
            SessionCentral.agents[company.id]!![actionWs.agent] = webSocketSession
        }
        else{
            SessionCentral.agents[company.id] = mutableMapOf(actionWs.agent to webSocketSession)
        }
        return company
    }

    private fun contactsHaveNewMessages(contacts: Flux<Contact>) = contacts
        .map { contact ->
            Optional.ofNullable(ContactCenter.contacts[contact.company])
                .map {
                    if (it.containsKey(contact.id)){
                        contact.newMessage = true
                        contact.newMessageQtde = it[contact.id]!!.message
                    }
                }
            contact
        }

}