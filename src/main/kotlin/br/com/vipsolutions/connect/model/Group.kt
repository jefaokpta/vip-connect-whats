package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.annotation.Transient

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */

@Table("groups")
data class Group(
    @Id
    val id: Long,
    val name: String,
    val controlNumber: Long
) {
    @Transient
    val contacts: List<Contact> = listOf()
}