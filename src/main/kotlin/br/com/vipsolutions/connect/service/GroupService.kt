package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.Group
import br.com.vipsolutions.connect.model.GroupDAO
import br.com.vipsolutions.connect.model.GroupMessage
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.GroupRepository
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val contactRepository: ContactRepository
) {

    fun getGroupWithContactList(id: Long) = groupRepository.findById(id)
        .map(::GroupDAO)

    fun newGroup(groupDAO: GroupDAO) = groupRepository.save(Group(groupDAO))
        .map(::GroupDAO)

    fun updateGroup(groupDAO: GroupDAO) = groupRepository.findById(groupDAO.id)
        .flatMap{groupRepository.save(Group(groupDAO))}

    fun deleteGroup(id: Long) = groupRepository.findById(id)
        .flatMap (groupRepository::delete)

    fun sendGroupMessage(groupMessage: GroupMessage) = groupRepository.findById(groupMessage.groupId)
        .doOnNext { runItAfter(groupMessage, it) }

    private fun runItAfter(groupMessage: GroupMessage, group: Group){
        println("Enviando mensagem para o grupo ${groupMessage.groupId}")
        if (group.contactsId.isEmpty()){
            return
        }
        contactRepository.findAllById(group.contactsId.split(",").map(String::toLong))
            .map { sendIndividualMessage(it, groupMessage) }
            .limitRate(1)
            .delayElements(Duration.ofSeconds(2))
            .subscribe()
    }

    private fun sendIndividualMessage(contact: Contact, groupMessage: GroupMessage) {
        println("Mandando Mensagem de Grupo '${groupMessage.message}' ${groupMessage.groupId} para ${contact.name} - ${contact.whatsapp}")
        sendTextMessage(contact.whatsapp, groupMessage.message, contact.instanceId)
    }

    fun getAllGroupsByControlNumber(controlNumber: Long) = groupRepository.findAllByControlNumber(controlNumber)
        .collectList()
}