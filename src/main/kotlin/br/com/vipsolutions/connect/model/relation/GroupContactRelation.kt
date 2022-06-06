package br.com.vipsolutions.connect.model.relation

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */
@Table("groups_contacts")
data class GroupContactRelation(
    @Id
    val id: Long,
    val groupId: Long,
    val contactId: Long,
) {
}