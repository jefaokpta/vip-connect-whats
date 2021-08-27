package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.getRobotGreeting
import br.com.vipsolutions.connect.client.getRobotUra
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
        return if (Optional.ofNullable(contact.category).isEmpty){
            getRobotGreeting(contact.company)
                .flatMap { robotResponseToContact(it.greet,contact, whatsChat) }
                .switchIfEmpty(Mono.just(contact))
                .flatMap { getRobotUra(contact.company) }
                .flatMap{handleRobotMessage(it, whatsChat, contact)}
                .switchIfEmpty (categorizedContact(contact.apply { category = 0 }, whatsChat))
                .log()
        } else{
            deliverMessageFlow(contact, whatsChat)
        }
    }

    private fun categorizedContact(contact: Contact, whatsChat: WhatsChat) = contactRepository.save(contact)
        .flatMap { deliverMessageFlow(it, whatsChat) }

    private fun handleRobotMessage(ura: Ura, whatsChat: WhatsChat, contact: Contact): Mono<Contact> {
        val answer = isAnswer(ura, whatsChat, contact)
        if (answer.isPresent){
            return robotResponseToContact(ura.thank, contact, whatsChat)
                .flatMap { categorizedContact(answer.get(), whatsChat) }
        }
        return robotResponseToContact(ura, contact, whatsChat)
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
            if (answer.answer.toString() == whatsChat.text) {
                return Optional.of(contact.apply { category = answer.answer })
            }
        }
        return Optional.empty()
    }

}