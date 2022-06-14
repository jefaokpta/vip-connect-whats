package br.com.vipsolutions.connect.model

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 14/06/22
 */
class ContactLite(
    val id: Long,
    val name: String?,
    val whatsapp: String,
    val imgUrl: String?,
) {
    constructor(contact: Contact) : this(
        contact.id,
        contact.name,
        contact.whatsapp,
        contact.imgUrl
    )
}