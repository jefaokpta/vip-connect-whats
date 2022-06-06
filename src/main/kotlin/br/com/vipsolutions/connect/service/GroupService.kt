package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.GroupContactRelationRepository
import br.com.vipsolutions.connect.repository.GroupRepository
import org.springframework.stereotype.Service

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupContactRelationRepository: GroupContactRelationRepository,
    private val contactRepository: ContactRepository
) {

    fun getGroupWithContactList(id: Long) = groupContactRelationRepository.findAllByGroupId(id)
        .collectList()
        .map { gcrList -> contactRepository.findAllById(gcrList.map { it.contactId })}
        .flatMap { it.collectList() }
        .flatMap { putContactListOnGroup(id, it) }

    private fun putContactListOnGroup(id: Long, contactList: List<Contact>) = groupRepository.findById(id)
        .map { it.apply { contacts = contactList } }
}