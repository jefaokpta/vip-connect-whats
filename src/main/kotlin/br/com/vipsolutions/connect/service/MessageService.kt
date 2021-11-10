package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.*
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.GreetingRepository
import br.com.vipsolutions.connect.repository.UraRepository
import br.com.vipsolutions.connect.util.*
import br.com.vipsolutions.connect.websocket.SessionCentral
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
                .switchIfEmpty (categorizedContact(contact.apply { category = 0; lastCategory = 0 }, whatsChat))
//                .log()
        } else{
            deliverMessageFlow(contact, whatsChat)
        }
    }

    private fun categorizedContact(contact: Contact, whatsChat: WhatsChat) = contactRepository.save(contact)
        .flatMap { deliverMessageFlow(it, whatsChat) }

    private fun handleRobotMessage(ura: Ura, whatsChat: WhatsChat, contact: Contact): Mono<Contact> {
        if (AnsweringUraCenter.contacts.containsKey(whatsChat.remoteJid)){
            val answer = isAnswer(ura, whatsChat, contact)
            if (answer.isPresent){
                AnsweringUraCenter.contacts.remove(whatsChat.remoteJid)
                return genericMessage(ura.validOption, answer.get())
                    .doOnNext { queryOnlineAgents(it) }
                    .flatMap { categorizedContact(it, whatsChat) }
            }
            return if(ura.invalidOption.isNullOrBlank()){
                buildUraMessageNoInitialMessage(ura, contact)
            }else Mono.just(sendTextMessage(contact.whatsapp, ura.invalidOption, contact.instanceId))
                .flatMap { buildUraMessageNoInitialMessage(ura, contact) }
        }
        AnsweringUraCenter.contacts[whatsChat.remoteJid] = ""
        Optional.ofNullable(AnsweringQuizCenter.quizzes.remove(contact.whatsapp))
            .map { sendQuizAnswerToVip(it, 0) }
        return buildUraMessage(ura, contact)
    }

    fun askContactName(remoteJid: String, company: Long, instanceId: Int, whatsChat: WhatsChat) = greetingRepository.findByCompany(company)
        .switchIfEmpty(greetingRepository.findByCompany(0))
        .flatMap { robotAskContactName(remoteJid, it, instanceId, whatsChat, company) }

    fun prepareContactToSave(remoteJid: String, company: Long, instanceId: Int, name: String): Mono<Contact> {
        val profilePicture = getProfilePicture(instanceId, remoteJid)
        if(profilePicture.picture !== null){
            return contactRepository.save(Contact(0, name, remoteJid, company, instanceId, profilePicture.picture,
                null, null, null, 0, null, false, false))
        }
        println("CAGOU AO PEGAR FOTO DO PERFIL ${profilePicture.errorMessage}")
        return contactRepository.save(Contact(0, name, remoteJid, company, instanceId, null, null,
            null, null, 0, null, false, false))
    }

    fun updateContactLastMessage(contact: Contact, datetime: LocalDateTime, messageId: String) = contactRepository.save(contact.apply {
        if (!busy) fromAgent = false
        lastMessageId = messageId
        lastMessageTime = datetime
    })

    private fun queryOnlineAgents(contact: Contact){
        SessionCentral.agents[contact.company]?.forEach { agent ->
            if (agent.value.categories.contains(contact.category)){
                return
            }
        }
        uraRepository.findByCompany(contact.company)
            .map { genericMessage(it.agentEmpty, contact) }
            .subscribe()
    }

    private fun deliverMessageFlow(contact: Contact, whatsChat: WhatsChat) = Mono.just(contact)
        .map { contactOnAttendance(it, whatsChat) }
        .map { addContactCenter(contact.company, it) }
        .flatMap { updateContactLastMessage(it, whatsChat.datetime, whatsChat.messageId) }
        .doFinally { alertNewMessageToAgents(contact).subscribe() }


    private fun genericMessage(message: String?, contact: Contact): Mono<Contact> {
        Optional.ofNullable(message)
            .map { sendTextMessage(contact.whatsapp, it, contact.instanceId) }
        return Mono.just(contact)
    }

    private fun robotAskContactName(remoteJid: String, greeting: Greeting, instanceId: Int, whatsChat: WhatsChat,company: Long): Mono<Contact>{
        if (WaitContactNameCenter.names.containsKey(remoteJid)){
            WaitContactNameCenter.names.remove(remoteJid)
            return prepareContactToSave(remoteJid, company, instanceId, whatsChat.text)
        } else {
            WaitContactNameCenter.names[remoteJid] = ""
            sendTextMessage(remoteJid, greeting.greet, instanceId)
        }
        return Mono.empty()
    }

    private fun buildUraMessage(ura: Ura, contact: Contact): Mono<Contact> {
        val stringBuilder = StringBuilder(ura.initialMessage)
        ura.options.forEach { stringBuilder.append("\n ${it.option} - ${it.department}") }
        sendTextMessage(contact.whatsapp, stringBuilder.toString(), contact.instanceId)
        return Mono.just(contact)
    }

    private fun buildUraMessageNoInitialMessage(ura: Ura, contact: Contact): Mono<Contact> {
        val stringBuilder = StringBuilder()
        ura.options.forEach { stringBuilder.append("\n${it.option} - ${it.department}") }
        sendTextMessage(contact.whatsapp, stringBuilder.toString().trim(), contact.instanceId)
        return Mono.just(contact)
    }

    private fun isAnswer(ura: Ura, whatsChat: WhatsChat, contact: Contact): Optional<Contact> {
        ura.options.forEach { answer ->
            if (answer.option.toString() == whatsChat.text) {
                return Optional.of(generateProtocol(contact.apply {
                    category = answer.departmentId
                    lastCategory = answer.departmentId
                }))
            }
        }
        return Optional.empty()
    }

}