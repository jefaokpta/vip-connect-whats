package br.com.vipsolutions.connect.model.dto

import br.com.vipsolutions.connect.model.Contact

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 14/06/22
 */
class ContactLite(
    val id: Long,
    val name: String?,
    val whatsapp: String,
    val imgUrl: String?,
    val isBlocked: Boolean = false,
) {
    constructor(contact: Contact) : this(
        contact.id,
        contact.name,
        contact.whatsapp,
        contact.imgUrl,
        contact.isBlocked
    )
}