package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@Table("whats_chats")
data class WhatsChat(
    @Id
    val messageId: String,
    val remoteJid: String,
    var text: String,
    val fromMe: Boolean,
    var status: Int,
    val datetime: LocalDateTime = LocalDateTime.now(),
    var media: Boolean = false,
    var mediaType: String?,
    var mediaUrl: String?,
    var mediaCaption: String?
): Persistable<String> {
    @Transient
    var isPersistable: Boolean = true

    override fun getId() = messageId

    override fun isNew() = isPersistable
}