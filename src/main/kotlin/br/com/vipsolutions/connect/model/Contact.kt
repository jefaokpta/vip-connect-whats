package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table

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
) {
    @Transient
    val busy = false
    @Transient
    var newMessage = false
    @Transient
    var newMessageQtde = 0
}