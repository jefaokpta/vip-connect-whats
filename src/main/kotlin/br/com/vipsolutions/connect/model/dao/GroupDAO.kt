package br.com.vipsolutions.connect.model.dao

import br.com.vipsolutions.connect.model.Group


/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 14/06/22
 */
class GroupDAO(
    val id: Long,
    val name: String,
    val controlNumber: Long,
    var contactsId: List<Long> = listOf()
) {
    constructor(group: Group) : this(
        group.id,
        group.name,
        group.controlNumber,
    ){
        this.contactsId = handleContactsIdBlank(group.contactsId)
    }

    private fun handleContactsIdBlank(contactsId: String): List<Long> {
        return if (contactsId.isBlank()) {
            listOf()
        } else {
            contactsId.split(",").map(String::toLong)
        }
    }
}