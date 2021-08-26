package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.getRobotMessage
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.util.addContactCenter
import br.com.vipsolutions.connect.websocket.alertNewMessageToAgents
import br.com.vipsolutions.connect.websocket.contactOnAttendance
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 26/08/21
 */
@Service
class MessageService(private val contactRepository: ContactRepository) {

    fun verifyMessageCategory(contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        println("VERIFICANDO CATEGORIA")
        return if (contact.category.isNullOrBlank()){
            getRobotMessage(contact.company)
                .flatMap{handleRobotMessage(it, whatsChat, contact)}
        } else{
            deliverMessageFlow(contact, whatsChat)
        }
    }

    fun prepareContactToSave(remoteJid: String, company: Long, instanceId: Int): Mono<Contact> {
        val profilePicture = getProfilePicture(instanceId, remoteJid)
        if(profilePicture.picture !== null){
            //println("IMAGEM DO PERFIL: ${profilePicture.picture}")
            return contactRepository.save(Contact(0, "Desconhecido", remoteJid, company, instanceId, profilePicture.picture, null, null, null))
        }
        println("CAGOU AO PEGAR FOTO DO PERFIL ${profilePicture.errorMessage}")
        return contactRepository.save(Contact(0, "Desconhecido", remoteJid, company, instanceId, null, null, null, null))
    }

    fun updateContactLastMessage(contact: Contact, datetime: LocalDateTime, messageId: String) = contactRepository.save(contact.apply {
        lastMessageId = messageId
        lastMessageTime = datetime
    })

    private fun deliverMessageFlow(contact: Contact, whatsChat: WhatsChat) = Mono.just(contact)
        .map { contactOnAttendance(it, whatsChat) }
        .map { addContactCenter(contact.company, it) }
        .flatMap { updateContactLastMessage(it, whatsChat.datetime, whatsChat.messageId) }
        .doFinally { alertNewMessageToAgents(contact).subscribe() }

    private fun handleRobotMessage(ura: Ura, whatsChat: WhatsChat, contact: Contact): Mono<Contact> {
        val answer = isAnswer(ura, whatsChat, contact)
        if (answer.isPresent){
            return robotResponseToContact(ura.thank, contact, whatsChat)
                .flatMap { contactRepository.save(answer.get()) }
                .flatMap { deliverMessageFlow(it, whatsChat) }
        }
        return robotResponseToContact(ura, contact, whatsChat)
    }

    private fun robotResponseToContact(message: String, contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        sendTextMessage(
            WhatsChat("", "", message, false, 0, whatsChat.datetime,
                false, null, null, null, null, null), contact)
        return Mono.just(contact)
    }

    private fun robotResponseToContact(ura: Ura, contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        val stringBuilder = StringBuilder(ura.greeting)
        ura.answers.forEach { stringBuilder.append("\n ${it.answer} para ${it.category}") }
        sendTextMessage(
            WhatsChat("", "", stringBuilder.toString(), false, 0, whatsChat.datetime,
                false, null, null, null, null, null), contact)
        return Mono.just(contact)
    }

    private fun isAnswer(ura: Ura, whatsChat: WhatsChat, contact: Contact): Optional<Contact> {
        ura.answers.forEach { answer ->
            if (answer.answer.equals(whatsChat.text, true)) {
                return Optional.of(contact.apply { category = answer.category })
            }
        }
        return Optional.empty()
    }

}