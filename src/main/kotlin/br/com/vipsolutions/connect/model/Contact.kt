package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@Table("contacts")
data class Contact(
    @Id
    val id: Long,
    val name: String?,
    val whatsapp: String,
    val company: Long,
    val instanceId: Int,
    var lastCategory: Long,
    var imgUrl: String? = null,
    var lastMessageId: String? = null,
    var lastMessageTime: LocalDateTime? = null,
    var category: Long? = null,
    var protocol: Long? = null,
    var fromAgent: Boolean = false,
    var isNewProtocol: Boolean = false,
) {
    @Transient
    var busy = false
    @Transient
    var newMessage = false
    @Transient
    var newMessageQtde = 0

    constructor(contact: Contact): this(
        contact.id,
        contact.name,
        contact.whatsapp,
        contact.company,
        contact.instanceId,
        contact.lastCategory,
        contact.imgUrl,
        contact.lastMessageId,
        contact.lastMessageTime,
        contact.category,
        contact.protocol,
        contact.fromAgent,
        contact.isNewProtocol
    )
}