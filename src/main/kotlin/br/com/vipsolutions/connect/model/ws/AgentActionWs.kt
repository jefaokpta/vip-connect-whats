package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.WhatsChat

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-03
 */
class AgentActionWs(
    var action: String,
    val agent: Int,
    val company: Int,
    var contacts: List<Contact>?,
    var contact: Contact?,
    var messages: List<WhatsChat>?,
    var message: WhatsChat?,
    var errorMessage: String?,
    val category: MutableList<Int> = mutableListOf()
) {
}