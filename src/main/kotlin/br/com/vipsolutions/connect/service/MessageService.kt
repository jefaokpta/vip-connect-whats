package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.*
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.GreetingRepository
import br.com.vipsolutions.connect.repository.UraRepository
import br.com.vipsolutions.connect.util.WaitContactNameCenter
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
class MessageService(
    private val contactRepository: ContactRepository,
    private val greetingRepository: GreetingRepository,
    private val uraRepository: UraRepository,
    private val uraOptionService: UraOptionService
) {

    fun verifyMessageCategory(contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        println("VERIFICANDO CATEGORIA")
        return if (Optional.ofNullable(contact.category).isEmpty){
            uraRepository.findByCompany(contact.company)
                .flatMap { uraOptionService.fillOptions(it) }
                .flatMap{handleRobotMessage(it, whatsChat, contact)}
                .switchIfEmpty (categorizedContact(contact.apply { category = 0 }, whatsChat))
//                .log()
        } else{
            deliverMessageFlow(contact, whatsChat)
        }
    }

    private fun categorizedContact(contact: Contact, whatsChat: WhatsChat) = contactRepository.save(contact)
        .flatMap { deliverMessageFlow(it, whatsChat) }

    private fun handleRobotMessage(ura: Ura, whatsChat: WhatsChat, contact: Contact): Mono<Contact> {
        val answer = isAnswer(ura, whatsChat, contact)
        if (answer.isPresent){
            return robotResponseToContact(ura.validOption, contact)
                .flatMap { categorizedContact(answer.get(), whatsChat) }
        }
        return robotResponseToContact(ura, contact)
    }

    fun askContactName(remoteJid: String, company: Long, instanceId: Int, whatsChat: WhatsChat) = greetingRepository.findByCompany(company)
        .switchIfEmpty(greetingRepository.findByCompany(0))
        .flatMap { robotAskContactName(remoteJid, it, instanceId, whatsChat) }
//        .flatMap { prepareContactToSave(remoteJid, company, instanceId) }
//        .log()

    fun prepareContactToSave(remoteJid: String, company: Long, instanceId: Int, name: String): Mono<Contact> {
        val profilePicture = getProfilePicture(instanceId, remoteJid)
        if(profilePicture.picture !== null){
            //println("IMAGEM DO PERFIL: ${profilePicture.picture}")
            return contactRepository.save(Contact(0, name, remoteJid, company, instanceId, profilePicture.picture, null, null, null))
        }
        println("CAGOU AO PEGAR FOTO DO PERFIL ${profilePicture.errorMessage}")
        return contactRepository.save(Contact(0, name, remoteJid, company, instanceId, null, null, null, null))
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


    private fun robotResponseToContact(message: String?, contact: Contact): Mono<Contact> {
        Optional.ofNullable(message)
            .map { sendTextMessage(contact.whatsapp, it, contact.instanceId) }
        return Mono.just(contact)
    }

    private fun robotAskContactName(remoteJid: String, greeting: Greeting, instanceId: Int, whatsChat: WhatsChat): Mono<Contact>{
        if (WaitContactNameCenter.names.containsKey(remoteJid)){
            WaitContactNameCenter.names[remoteJid] = whatsChat.text
            sendButtonsMessage(remoteJid, whatsChat.text, instanceId, greeting)
        } else {
            WaitContactNameCenter.names[remoteJid] = ""
            sendTextMessage(remoteJid, greeting.greet, instanceId)
        }
        return Mono.empty()
    }

    private fun robotResponseToContact(ura: Ura, contact: Contact): Mono<Contact> {
        val stringBuilder = StringBuilder(ura.initialMessage)
        ura.options.forEach { stringBuilder.append("\n ${it.option} para ${it.department}") }
        sendTextMessage(contact.whatsapp, stringBuilder.toString(), contact.instanceId)
        return Mono.just(contact)
    }

    private fun isAnswer(ura: Ura, whatsChat: WhatsChat, contact: Contact): Optional<Contact> {
        ura.options.forEach { answer ->
            if (answer.option.toString() == whatsChat.text) {
                return Optional.of(contact.apply { category = answer.option })
            }
        }
        return Optional.empty()
    }

}