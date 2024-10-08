package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.sendMediaMessage
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.*
import br.com.vipsolutions.connect.model.dto.GroupDTO
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
        .map(::GroupDTO)

    fun newGroup(groupDTO: GroupDTO) = groupRepository.save(Group(groupDTO))
        .map(::GroupDTO)

    fun updateGroup(groupDTO: GroupDTO) = groupRepository.findById(groupDTO.id)
        .flatMap{groupRepository.save(Group(groupDTO))}

    fun deleteGroup(id: Long) = groupRepository.findById(id)
        .flatMap (groupRepository::delete)

    fun sendGroupMessage(groupMessage: GroupMessage) = groupRepository.findById(groupMessage.groupId)
        .doOnNext { runItAfter(groupMessage, it) }

    fun sendGroupMessage(fileUpload: FileUpload) = groupRepository.findById(fileUpload.messageGroupId!!)
        .doOnNext { runItAfter(it, fileUpload ) }

    private fun runItAfter(groupMessage: GroupMessage, group: Group){
        println("Enviando mensagem para o grupo ${groupMessage.groupId}")
        if (group.contactsId.isEmpty()){
            return
        }
        contactRepository.findAllById(group.contactsId.split(",").map(String::toLong))
            .limitRate(1)
            .delayElements(Duration.ofSeconds(2))
            .map { sendIndividualMessage(it, groupMessage) }
            .subscribe()
    }

    private fun runItAfter(group: Group, fileUpload: FileUpload){
        println("Enviando Media para o grupo ${fileUpload.messageGroupId}")
        if (group.contactsId.isEmpty()){
            return
        }
        contactRepository.findAllById(group.contactsId.split(",").map(String::toLong))
            .limitRate(1)
            .delayElements(Duration.ofSeconds(2))
            .flatMap { sendMediaMessage(fileUpload.copy(remoteJid = it.whatsapp, instanceId = it.instanceId)) }
            .subscribe()
    }

    private fun sendIndividualMessage(contact: Contact, groupMessage: GroupMessage) {
        println("Mandando Mensagem de Grupo '${groupMessage.message}' ${groupMessage.groupId} para ${contact.name} - ${contact.whatsapp}")
        sendTextMessage(contact.whatsapp, groupMessage.message, contact.instanceId)
    }

    fun getAllGroupsByControlNumber(controlNumber: Long) = groupRepository.findAllByControlNumber(controlNumber)
        .collectList()
}