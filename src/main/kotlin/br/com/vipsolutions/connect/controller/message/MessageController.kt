package br.com.vipsolutions.connect.controller.message

import br.com.vipsolutions.connect.client.sendQuizAnswerToVip
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.MessageStatus
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import br.com.vipsolutions.connect.service.MessageService
import br.com.vipsolutions.connect.service.WsChatHandlerService
import br.com.vipsolutions.connect.util.AnsweringQuizCenter
import br.com.vipsolutions.connect.websocket.contactOnAttendance
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val whatsChatRepository: WhatsChatRepository,
    private val contactRepository: ContactRepository,
    private val messageService: MessageService,
    private val wsChatHandlerService: WsChatHandlerService,
) {

    @PostMapping
    fun received(@RequestBody payload: String): Mono<WhatsChat> {
        println("RECEBIDO: $payload")
        val jsonObject = Gson().fromJson(payload, JsonObject::class.java)
        val remoteJid = jsonObject.getAsJsonObject("key")["remoteJid"].asString
        val messageId = jsonObject.getAsJsonObject("key")["id"].asString
        val fromMe = jsonObject.getAsJsonObject("key")["fromMe"].asBoolean
        val status = jsonObject["status"].asInt
        val company = jsonObject["company"].asLong
        val instanceId = jsonObject["instanceId"].asInt
        val datetime = LocalDateTime.now()

        val whatsChat = WhatsChat(messageId, remoteJid, "", fromMe, status, company, datetime)
        if(jsonObject["mediaMessage"].asBoolean){
            whatsChat.media = true
            whatsChat.mediaType = jsonObject["mediaType"].asString
            whatsChat.mediaUrl = jsonObject["mediaUrl"].asString.substringAfterLast("/")
            if(jsonObject.has("mediaFileLength")){
                whatsChat.mediaFileLength = jsonObject.getAsJsonObject("mediaFileLength")["low"].asLong
            }
            if(jsonObject.has("mediaPageCount")){
                whatsChat.mediaPageCount = jsonObject["mediaPageCount"].asInt
            }
            if(jsonObject.has("mediaCaption")){
                whatsChat.mediaCaption = jsonObject["mediaCaption"].asString
            }
            if(jsonObject.has("mediaFileTitle")){
                whatsChat.mediaCaption = jsonObject["mediaFileTitle"].asString
            }
        }
        else{
            if (jsonObject["message"].isJsonNull){
                println("ITERACAO DO WHATS SEM MENSAGEM")
                return Mono.empty()
            }
            jsonObject.addProperty("error", "Erro: Não encontrado texto ou conversa.")
            val textJson = jsonObject.getAsJsonObject("message")["conversation"]?:
            jsonObject.getAsJsonObject("message").getAsJsonObject("extendedTextMessage")["text"]?:
            jsonObject["error"]
            whatsChat.text = textJson.asString
        }


        return if(fromMe){
            contactRepository.findByWhatsappAndCompany(whatsChat.remoteJid, company)
                .map { contactOnAttendance(it, whatsChat.apply { protocol = it.protocol })}
                .flatMap { messageService.updateContactLastMessage(it, datetime, messageId) }
                .flatMap { whatsChatRepository.findById(messageId) }
                .flatMap { dbWhatsChat ->
                    dbWhatsChat.status = whatsChat.status
                    dbWhatsChat.protocol = whatsChat.protocol
                    dbWhatsChat.isPersistable = false
                    whatsChatRepository.save(dbWhatsChat)
                }
                .switchIfEmpty(whatsChatRepository.save(whatsChat))
        }
        else {
            contactRepository.findByWhatsappAndCompany(remoteJid, company)
                .switchIfEmpty(Mono.defer { messageService.askContactName(remoteJid, company, instanceId, whatsChat) })
                .flatMap { messageService.verifyMessageCategory(it, whatsChat.apply { protocol = it.protocol }) }
                .switchIfEmpty(Mono.just(Contact(0, "", "", 0, 0, 0)))
                .flatMap { whatsChatRepository.save(whatsChat) }
//                .log()
        }
    }

    @PostMapping("/status/update")
    fun updateOutgoingMessageStatus(@RequestBody messageStatus: MessageStatus) = whatsChatRepository.findById(messageStatus.id)
        .flatMap { whatsChatRepository.save(it.apply { status = messageStatus.status; isPersistable = false }) }
        .then()

    @PostMapping("/responses")
    fun buttonsResponse(@RequestBody payload: String): Mono<Void> {
        println("RESPOSTA: $payload")
        val jsonObject = Gson().fromJson(payload, JsonObject::class.java)
        val selectedBtn = jsonObject.getAsJsonObject("message")
            .getAsJsonObject("buttonsResponseMessage")["selectedButtonId"].asInt
        val remoteJid = jsonObject.getAsJsonObject("key")["remoteJid"].asString
        val company = jsonObject["company"].asLong
        val instanceId = jsonObject["instanceId"].asInt

        return Optional.ofNullable(AnsweringQuizCenter.quizzes[remoteJid])
            .map {
                sendQuizAnswerToVip(it, selectedBtn)
                AnsweringQuizCenter.quizzes.remove(remoteJid)
                println("QUIZ RESPONDIDO $remoteJid")
                it
            }
            .map { wsChatHandlerService.finalizeAttendance(it.contact) }
            .orElse(Mono.empty())
            .then()
    }

    @GetMapping("/{company}/{remoteJid}")
    fun chats(@PathVariable remoteJid: String, @PathVariable company: Long): Flux<WhatsChat> {
        return whatsChatRepository.findTop500ByRemoteJidAndCompanyOrderByDatetimeDesc(remoteJid, company)
    }
}