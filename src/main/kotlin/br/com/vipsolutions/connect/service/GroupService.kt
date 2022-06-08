package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.Group
import br.com.vipsolutions.connect.model.relation.GroupContactRelation
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.GroupContactRelationRepository
import br.com.vipsolutions.connect.repository.GroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Transactional
    fun newGroup(group: Group) = groupRepository.save(group)
        .flatMap { insertGroupContactRelation(it, group.contactsId) }

    private fun insertGroupContactRelation(group: Group, contactsIdReceived: List<Long>) = groupContactRelationRepository.saveAll(
        contactsIdReceived.map { GroupContactRelation(group.id, it) })
        .collectList()
        .map { group.apply { contactsId = it.map { it.contactId } } }

    private fun putContactListOnGroup(id: Long, contactList: List<Contact>) = groupRepository.findById(id)
        .map { it.apply { contacts = contactList } }
}