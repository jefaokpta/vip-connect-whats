package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.dao.GroupDAO
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
    val controlNumber: Long,
    val contactsId: String = "",
) {
    @Transient
    var contacts: List<Contact> = listOf()

    constructor(groupDAO: GroupDAO) : this(
        groupDAO.id,
        groupDAO.name,
        groupDAO.controlNumber,
        groupDAO.contactsId.joinToString(",")
    ){
    }
}