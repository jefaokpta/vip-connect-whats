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
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class SessionCentral {

    companion object {
        private val agents = ConcurrentHashMap<Long, MutableMap<Int, AgentSession>>()
        fun getAllByCompanyId(companyId: Long): Map<Int, AgentSession>? {
            return agents[companyId]
        }

        fun getAll() = agents.values.map{it.keys}.flatten()

        fun verifyLockedContacts(contactsAndId: ContactsAndId): List<Contact> {
            val contactsBusy = mutableMapOf<Long, Int>()
            agents[contactsAndId.companyId]?.values?.forEach {
                if (it.contact !== null) {
                    contactsBusy[it.contact!!.id] = 0
                }
            } ?: return contactsAndId.contacts

            contactsAndId.contacts.forEach { contact ->
                if (contactsBusy.containsKey(contact.id)) {
                    contact.busy = true
                }
            }
            return contactsAndId.contacts
        }

        @Synchronized
        fun unlockContact(contact: Contact, agent: Int): Flux<Void> {
            val agentSession = agents[contact.company]?.get(agent) ?: return Flux.empty()
            if (agentSession.contact !== null) {
                val contactCopy = Contact(agentSession.contact!!)
                contactCopy.busy = false
                agentSession.contact = null
                return broadcastToAgents(contactCopy, "UNLOCK_CONTACT")
            }
            return Flux.empty()
        }


        @Synchronized
        fun lockContact(contact: Contact, agent: Int): Flux<Void> {
            val agentSession = agents[contact.company]?.get(agent) ?: return Flux.empty()
            agentSession.contact = contact
            contact.busy = true
            return broadcastToAgents(contact, "LOCK_CONTACT")
        }

        fun broadcastToAgents(contact: Contact, action: String) = Optional.ofNullable(agents[contact.company])
            .map { Flux.fromIterable(it.values) }
            .orElse(Flux.empty())
            .flatMap {
                try {
                    println("👁 ENVIANDO BROADCAST PRA AGENTES ${it.session.id}")
                    it.session.send(
                        Mono.just(
                            it.session.textMessage(
                                objectToJson(
                                    AgentActionWs(
                                        action,
                                        0,
                                        0,
                                        null,
                                        null,
                                        contact,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                    )
                                )
                            )
                        )
                    )
                }
                catch (ex: Exception){
                    println("🧨 DEU RUIM AO ENVIAR BROADCAST PRA AGENTES ${it.session.id} - ${ex.message}")
                    Flux.empty()
                }
            }

        fun clearNewMessageToAgents(contact: Contact): Flux<Void> {
            return broadcastToAgents(contact.apply { newMessage = false; newMessageQtde = 0 }, "CONTACT_READED")
        }

        @Synchronized
        fun addAgentSession(company: Company, actionWs: AgentActionWs, webSocketSession: WebSocketSession): Company {
            if (agents.contains(company.id)) {
                agents[company.id]!![actionWs.agent] = AgentSession(webSocketSession, null, actionWs.categories)
            } else {
                agents[company.id] =
                    mutableMapOf(actionWs.agent to AgentSession(webSocketSession, null, actionWs.categories))
            }
            return company
        }

        @Synchronized
        fun removeAgentSession(session: WebSocketSession) {
            val agentsMap = HashMap(agents)
            agentsMap.forEach { companyMap ->
                companyMap.value.forEach { sessionMap ->
                    if (session == sessionMap.value.session) {
                        agents[companyMap.key]?.remove(sessionMap.key)
                        println("removido agente chave ${sessionMap.key} do mapa id ${companyMap.key} - ${LocalDateTime.now()}")
                        return
                    }
                }
            }
        }

        fun contactOnAttendance(contact: Contact, whatsChat: WhatsChat): Contact {
            if (whatsChat.text == "#") {
                forceUnlockContactByContact(contact)
                return contact.apply { busy = false }
            }
            val agents = agents[contact.company] ?: return contact
            agents.forEach { agent ->
                if (agent.value.contact !== null) {
                    if (agent.value.contact!!.id == contact.id) {
                        contact.busy = true
                        agent.value.session.send(
                            Mono.just(
                                agent.value.session.textMessage(
                                    objectToJson(
                                        AgentActionWs(
                                            "MESSAGE_IN_ATTENDANCE",
                                            0,
                                            0,
                                            null,
                                            null,
                                            contact,
                                            null,
                                            whatsChat,
                                            null,
                                            null,
                                            null,
                                            null
                                        )
                                    )
                                )
                            )
                        )
                            .subscribe()
                    }
                }
            }
            return contact
        }

        @Synchronized
        private fun forceUnlockContactByContact(contact: Contact) {
            agents[contact.company]?.forEach { agent ->
                if (agent.value.contact?.id == contact.id) {
                    agent.value.contact = null
                }
            }
        }

        fun alertNewMessageToAgents(contact: Contact): Flux<Void> {
            if (!contact.busy) {
                return Optional.ofNullable(agents[contact.company])
                    .map { agentSession ->
                        Flux.fromIterable(agentSession.values).filter { it.categories.contains(contact.category) }
                    }
                    .orElse(Flux.empty())
                    .flatMap {
                        try{
                            println("👁 ENVIANDO ALERTA DE NOVA MENSAGEM PRA AGENTES ${it.session.id}")
                            it.session.send(
                                Mono.just(
                                    it.session.textMessage(
                                        objectToJson(
                                            AgentActionWs(
                                                "NEW_MESSAGE",
                                                0,
                                                0,
                                                null,
                                                null,
                                                contact,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                            )
                                        )
                                    )
                                )
                            )
                        } catch (ex: Exception){
                            println("🧨 DEU RUIM AO ENVIAR ALERTA DE NOVA MENSAGEM PRA AGENTES ${it.session.id} - ${ex.message}")
                            Flux.empty()
                        }
                    }

            }
            return Flux.empty()
        }
    }
}

