package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.*
import br.com.vipsolutions.connect.model.dao.GroupDAO

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-03
 */
class AgentActionWs(
    var action: String,
    val agent: Int,
    val controlNumber: Long,
    var contacts: List<Contact>? = null,
    var contactsLite: List<ContactLite>? = null,
    var contact: Contact? = null,
    var messages: List<WhatsChat>? = null,
    var message: WhatsChat? = null,
    var errorMessage: String? = null,
    var group: GroupDAO? = null,
    var groups: List<GroupDAO>? = null,
    val groupMessage: GroupMessage? = null,
    val categories: MutableList<Long> = mutableListOf(),
    val categoryName: String? = null,
) {
}