package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import br.com.vipsolutions.connect.util.addContactCenter
import br.com.vipsolutions.connect.websocket.alertNewMessageToAgents
import br.com.vipsolutions.connect.websocket.contactOnAttendance
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val whatsChatRepository: WhatsChatRepository,
    private val contactRepository: ContactRepository
) {

    @PostMapping("/test")
    fun sendingTextMessageToNode(@RequestBody json: String){
        sendTextMessage(json, 3001)
    }
    @PostMapping //@Transactional
    fun received(@RequestBody payload: String): Mono<WhatsChat> {
        //println(payload)
        val jsonObject = Gson().fromJson(payload, JsonObject::class.java)
        val remoteJid = jsonObject.getAsJsonObject("key")["remoteJid"].asString
        val messageId = jsonObject.getAsJsonObject("key")["id"].asString
        val fromMe = jsonObject.getAsJsonObject("key")["fromMe"].asBoolean
        val timestamp = jsonObject.getAsJsonObject("messageTimestamp")["low"].asLong
        val status = jsonObject["status"].asInt
        val company = jsonObject["company"].asLong
        val instanceId = jsonObject["instanceId"].asInt

        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("-03:00"))
        //println(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date.from(datetime.toInstant(ZoneOffset.of("-03:00")))))

        val whatsChat = WhatsChat(messageId, remoteJid, "", fromMe, status, datetime, false, null, null, null, null, null)
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
            whatsChatRepository.findById(messageId)
                .flatMap { dbWhatsChat ->
                    dbWhatsChat.status = whatsChat.status
                    dbWhatsChat.isPersistable = false
                    whatsChatRepository.save(dbWhatsChat)
                }
                .switchIfEmpty(whatsChatRepository.save(whatsChat))
                .doFinally {
                    contactRepository.findByWhatsapp(whatsChat.remoteJid)
                        .map { contactOnAttendance(it, whatsChat)}
                        .flatMap { updateContactLastMessage(it, datetime, messageId) }
                        .subscribe()
                }

        }
        else {
            contactRepository.findByWhatsapp(remoteJid)
                .switchIfEmpty(Mono.defer { prepareContactToSave(remoteJid, company, instanceId) })
                //.map { sendTextMessage(WhatsChat("", "", "RECEBI SIM", false, 0, datetime, false, null, null, null, null, null), it); it }
                .flatMap { verifyMessageCategory(it, whatsChat) }
//                .map { contactOnAttendance(it, whatsChat)}
//                .map { addContactCenter(company, it) }
//                .flatMap { updateContactLastMessage(it, datetime, messageId) }
//                .map { alertNewMessageToAgents(it).subscribe() }
                .flatMap { whatsChatRepository.save(whatsChat) }
        }
    }

    private fun verifyMessageCategory(contact: Contact, whatsChat: WhatsChat): Mono<Contact> {
        println("VERIFICANDO CATEGORIA")
        if (contact.category.isNullOrBlank()){
            sendTextMessage(WhatsChat(
                "",
                "",
                "PRIMEIRO CONTATO",
                false,
                0,
                whatsChat.datetime,
                false,
                null,
                null,
                null,
                null,
                null
            ), contact)
            return Mono.empty()
        }
        else{
            return Mono.just(contact)
                .map { contactOnAttendance(it, whatsChat)}
                .map { addContactCenter(contact.company, it) }
                .flatMap { updateContactLastMessage(it, whatsChat.datetime, whatsChat.messageId) }
                .doFinally { alertNewMessageToAgents(contact).subscribe() }
                //.map { alertNewMessageToAgents(it) }
        }

    }

    private fun updateContactLastMessage(contact: Contact, datetime: LocalDateTime, messageId: String) = contactRepository.save(contact.apply {
        lastMessageId = messageId
        lastMessageTime = datetime
    })

    private fun prepareContactToSave(remoteJid: String, company: Long, instanceId: Int): Mono<Contact> {
        val profilePicture = getProfilePicture(instanceId, remoteJid)
        if(profilePicture.picture !== null){
            //println("IMAGEM DO PERFIL: ${profilePicture.picture}")
            return contactRepository.save(Contact(0, "Desconhecido", remoteJid, company, instanceId, profilePicture.picture, null, null, null))
        }
        println("CAGOU AO PEGAR FOTO DO PERFIL ${profilePicture.errorMessage}")
        return contactRepository.save(Contact(0, "Desconhecido", remoteJid, company, instanceId, null, null, null, null))
    }

    @GetMapping("/{remoteJid}")
    fun chats(@PathVariable remoteJid: String, @RequestParam("limit") limit: Int): Flux<WhatsChat> {
        return whatsChatRepository.findTop50ByRemoteJidOrderByDatetimeDesc(remoteJid)
    }
}