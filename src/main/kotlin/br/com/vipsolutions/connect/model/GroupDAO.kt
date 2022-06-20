package br.com.vipsolutions.connect.model


/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 14/06/22
 */
class GroupDAO(
    val id: Long,
    val name: String,
    val controlNumber: Long,
    val contacts: List<ContactLite> = listOf(),
    val contactsId: List<Long> = listOf()
) {
    constructor(group: Group) : this(
        group.id,
        group.name,
        group.controlNumber,
        group.contacts.map(::ContactLite),
        group.contactsId
    )
}