package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.GroupMessage
import br.com.vipsolutions.connect.repository.GroupRepository
import br.com.vipsolutions.connect.service.GroupService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 08/06/22
 */

@RestController
@RequestMapping("/api/groups/messages")
class GroupMessageController(
    private val groupService: GroupService,
    private val groupRepository: GroupRepository
) {
/*
    @PostMapping
    fun sendGroupMessage(@RequestBody groupMessage: GroupMessage) = groupRepository.findById(groupMessage.groupId)
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado")))
        .doOnNext { runItAfter(groupMessage) }
        .then()

    private fun runItAfter(groupMessage: GroupMessage){
        println("Enviando mensagem para o grupo ${groupMessage.groupId}")
        groupService.getGroupWithContactList(groupMessage.groupId)
            .doOnNext { sendIndividualMessage(it.contacts, groupMessage) }
            .subscribe()
    }

    private fun sendIndividualMessage(contacts: List<Contact>, groupMessage: GroupMessage) {
        contacts.forEach { contact ->
            println("Mandando Mensagem de Grupo ${groupMessage.groupId} para ${contact.name} - ${contact.whatsapp}")
            sendTextMessage(contact.whatsapp, groupMessage.message, contact.instanceId)
            TimeUnit.SECONDS.sleep(2)
        }
    }
    */
}