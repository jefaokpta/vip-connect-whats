package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
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
    val customerId: Long?,
    val fullName: String?,
    val residentialPhone: String?,
    val primaryEmail: String?,
    val secondaryEmail: String?,
    val whatsapp: String?,
    val facebook: String?,
    val instagram: String?,
    val telegram: String?,
    var deleted: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
}