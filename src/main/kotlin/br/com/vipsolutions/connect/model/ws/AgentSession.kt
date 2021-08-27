package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.Contact
import org.springframework.web.reactive.socket.WebSocketSession

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-22
 */
class AgentSession(
    val session: WebSocketSession,
    var contact: Contact?,
    val category: MutableList<Int> = mutableListOf()
) {
}