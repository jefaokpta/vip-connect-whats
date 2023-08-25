package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.sendQuizAnswerToVip
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.GreetingRepository
import br.com.vipsolutions.connect.repository.UraRepository
import br.com.vipsolutions.connect.util.*
import br.com.vipsolutions.connect.websocket.SessionCentral
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 26/08/21
 */
@Service
class MessageService(
    private val contactRepository: ContactRepository,
    private val greetingRepository: GreetingRepository,
    private val uraRepository: UraRepository,
    private val uraOptionService: UraOptionService,
    private val wsChatHandlerService: WsChatHandlerService
) {

    fun verifyMessageCategory(contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        return if (Optional.ofNullable(contact.category).isEmpty){
            handleRobotMessage(whatsChat, contact)
        } else{
            deliverMessageFlow(contact, whatsChat)
        }
    }

    private fun categorizedContact(contact: Contact, whatsChat: WhatsChat) = contactRepository.save(generateProtocol(contact))
        .flatMap { deliverMessageFlow(it, whatsChat) }
        .doOnNext { queryOnlineAgents(it) }

    private fun handleRobotMessage(whatsChat: WhatsChat, contact: Contact): Mono<Contact> {
        if (AnsweringUraCenter.containsUraAnswer(contact)){
            val uraAnswer = AnsweringUraCenter.getUraAnswer(contact)
            val answer = isAnswer(uraAnswer.ura, whatsChat, contact)
            if (answer.isPresent){
                val contactAccerted = answer.get()
                if (!contactAccerted.subUra.isNullOrBlank()){
                    return uraRepository.findByCompanyAndVipUraId(contactAccerted.company, contactAccerted.subUra!!.split("-")[1].toLong())
                        .flatMap { uraOptionService.fillOptions(it) }
                        .doOnNext { AnsweringUraCenter.addUraAnswer(contactAccerted, it) }
                        .flatMap { buildUraMessage(it, contactAccerted, whatsChat) }
                }
                AnsweringUraCenter.removeUraAnswer(contactAccerted)
                genericMessage(uraAnswer.ura.validOption, contactAccerted)
                return categorizedContact(contactAccerted, whatsChat)
            }
            AnsweringUraCenter.plusUraAnswerCounter(contact)
            println("URA OPCAO INVALIDA NUMERO: ${AnsweringUraCenter.getUraAnswerCounter(contact)}")
            if (AnsweringUraCenter.getUraAnswerCounter(contact) > 5){
                if (AnsweringUraCenter.getUraAnswerCounter(contact) > 10){
                    println("URA OPCAO INVALIDAS - ZERANDO TENTATIVAS")
                    AnsweringUraCenter.removeUraAnswer(contact)
                    return Mono.just(contact)
                }
                println("URA OPCAO INVALIDAS - BOT IGNORANDO")
                return Mono.just(contact)
            }
            return if(uraAnswer.ura.invalidOption.isNullOrBlank()){
                buildUraMessageNoInitialMessage(uraAnswer.ura, contact)
            }else Mono.just(sendTextMessage(contact.whatsapp, uraAnswer.ura.invalidOption, contact.instanceId))
                .delayElement(Duration.ofSeconds(2))
                .flatMap { buildUraMessageNoInitialMessage(uraAnswer.ura, contact) }
        }
        Optional.ofNullable(AnsweringQuizCenter.quizzes.remove(contact.whatsapp))
            .map { sendQuizAnswerToVip(it, 0) }
        return uraRepository.findTop1ByCompanyAndActive(contact.company)
            .flatMap { uraOptionService.fillOptions(it) }
            .doOnNext { AnsweringUraCenter.addUraAnswer(contact, it) }
            .flatMap { buildUraMessage(it, contact, whatsChat) }
            .switchIfEmpty (categorizedContact(contact.apply { category = 0; lastCategory = 0 }, whatsChat))
    }

    fun askContactName(remoteJid: String, company: Long, instanceId: Int, whatsChat: WhatsChat) = greetingRepository.findByCompany(company)
        .switchIfEmpty(greetingRepository.findByCompany(0))
        .flatMap { robotAskContactName(remoteJid, it, instanceId, whatsChat, company) }

    fun prepareContactToSave(remoteJid: String, company: Long, instanceId: Int, name: String): Mono<Contact> {
        val profilePicture = getProfilePicture(instanceId, remoteJid)
        if(profilePicture.picture !== null){
            return contactRepository.save(Contact(0, name, remoteJid, company, instanceId, 0, profilePicture.picture))
        }
        println("CAGOU AO PEGAR FOTO DO PERFIL ${profilePicture.errorMessage}")
        return contactRepository.save(Contact(0, name, remoteJid, company, instanceId, 0))
    }

    fun updateContactLastMessage(contact: Contact, datetime: LocalDateTime, messageId: String) = contactRepository.save(contact.apply {
        if (!busy) fromAgent = false
        lastMessageId = messageId
        lastMessageTime = datetime
    })

    private fun queryOnlineAgents(contact: Contact){
        SessionCentral.getAllByCompanyId(contact.company)?.forEach { agent ->
            if (agent.value.categories.contains(contact.category)){
                return
            }
        }
        uraRepository.findTop1ByCompanyAndActive(contact.company)
            .map { genericMessage(it.agentEmpty, contact) }
            .subscribe()
    }

    private fun deliverMessageFlow(contact: Contact, whatsChat: WhatsChat) = Mono.just(contact)
        .map { SessionCentral.contactOnAttendance(it, whatsChat) }
        .map { ContactCenter.addContactCenter(contact.company, it) }
        .flatMap{verifyClientRequestToFinalize(contact, whatsChat)}
        .flatMap { updateContactLastMessage(it, whatsChat.datetime, whatsChat.messageId) }
        .publishOn(Schedulers.boundedElastic())
        .doFinally { SessionCentral.alertNewMessageToAgents(contact).subscribe() }

    private fun verifyClientRequestToFinalize(contact: Contact, whatsChat: WhatsChat) = if (whatsChat.text == "#"){
        ContactCenter.remove(contact.company, contact.id)
        wsChatHandlerService.sendQuizOrFinalizeMsg(contact)
            .publishOn(Schedulers.boundedElastic())
            .doFinally { SessionCentral.broadcastToAgents(contact, "FINALIZE_ATTENDANCE").subscribe() }
    } else Mono.just(contact)

    private fun genericMessage(message: String?, contact: Contact): Contact {
        Optional.ofNullable(message)
            .map { sendTextMessage(contact.whatsapp, it, contact.instanceId) }
        return contact
    }

    private fun robotAskContactName(remoteJid: String, greeting: Greeting, instanceId: Int, whatsChat: WhatsChat,company: Long): Mono<Contact>{
        if (WaitContactNameCenter.names.containsKey(remoteJid)){
            WaitContactNameCenter.names.remove(remoteJid)
            return prepareContactToSave(remoteJid, company, instanceId, whatsChat.text)
        } else {
            WaitContactNameCenter.names[remoteJid] = ""
            CompletableFuture.runAsync {
                println("ROBOT ASK CONTACT NAME DELAY")
                TimeUnit.SECONDS.sleep(15)
                sendTextMessage(remoteJid, greeting.greet, instanceId)
            }
        }
        return Mono.empty()
    }

    private fun buildUraMessage(ura: Ura, contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        val stringBuilder = StringBuilder(ura.initialMessage)
        if (ura.options.isEmpty()){
            return uraWithoutOptions(contact, stringBuilder, whatsChat)
        }
        ura.options.forEach { stringBuilder.append("\n ${it.option} - ${it.department}") }

        CompletableFuture.runAsync {
            println("ROBOT ASK URA DELAY")
            TimeUnit.SECONDS.sleep(15)
            sendTextMessage(contact.whatsapp, stringBuilder.toString(), contact.instanceId)
        }
        return Mono.just(contact)
    }

    private fun uraWithoutOptions(contact: Contact, stringBuilder: StringBuilder, whatsChat: WhatsChat): Mono<Contact> {
        AnsweringUraCenter.removeUraAnswer(contact)
        contact.category = 0
        contact.lastCategory = 0
        CompletableFuture.runAsync {
            println("ROBOT ASK URA NO OPTIONS DELAY")
            TimeUnit.SECONDS.sleep(15)
            sendTextMessage(contact.whatsapp, stringBuilder.toString(), contact.instanceId)
        }
        return categorizedContact(contact, whatsChat)
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
                return Optional.of(contact.apply {
                    if (answer.departmentId.contains("subura")){
                        subUra = answer.departmentId
                    } else{
                        category = answer.departmentId.toLong()
                        lastCategory = answer.departmentId.toLong()
                    }
                })
            }
        }
        return Optional.empty()
    }

}