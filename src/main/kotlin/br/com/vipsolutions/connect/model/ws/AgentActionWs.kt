package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ContactLite
import br.com.vipsolutions.connect.model.GroupDAO
import br.com.vipsolutions.connect.model.WhatsChat

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
    val categories: MutableList<Long> = mutableListOf()
) {
}