package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-03
 */
class AgentActionWs(
    var action: String,
    val agent: Int,
    val controlNumber: Long,
    var contacts: List<Contact>?,
    var contactsLite: List<ContactLite>?,
    var contact: Contact?,
    var messages: List<WhatsChat>?,
    var message: WhatsChat?,
    var errorMessage: String?,
    var group: GroupDAO?,
    var groups: List<GroupDAO>?,
    val groupMessage: GroupMessage?,
    val categories: MutableList<Long> = mutableListOf()
) {
}