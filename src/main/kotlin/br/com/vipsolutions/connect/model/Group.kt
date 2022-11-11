package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.dto.GroupDTO
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

    constructor(groupDTO: GroupDTO) : this(
        groupDTO.id,
        groupDTO.name,
        groupDTO.controlNumber,
        groupDTO.contactsId.joinToString(",")
    ){
    }
}