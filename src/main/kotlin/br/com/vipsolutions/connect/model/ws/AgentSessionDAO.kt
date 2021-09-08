package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.Contact
import org.springframework.web.reactive.socket.WebSocketSession

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-22
 */
class AgentSessionDAO(
    val session: String,
    var contact: Contact?,
    val category: MutableList<Long> = mutableListOf()
) {
    constructor(agentSession: AgentSession): this (
        agentSession.session.id,
        agentSession.contact,
        agentSession.category
    )
}