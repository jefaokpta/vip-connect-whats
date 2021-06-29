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

        jsonObject.addProperty("error", "Erro: Não encontrado texto ou conversa.")
        val text = jsonObject.getAsJsonObject("message")["conversation"]?:
            jsonObject.getAsJsonObject("message").getAsJsonObject("extendedTextMessage")["text"]?:
            jsonObject["error"]

        val whatsChat = WhatsChat(messageId, remoteJid, text.asString, fromMe, status, datetime)
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
                        .subscribe()
                }

        }
        else {
            contactRepository.findByWhatsapp(remoteJid)
                .switchIfEmpty(Mono.defer { prepareContactToSave(remoteJid, company, instanceId) })
                .map { contactOnAttendance(it, whatsChat)}
                .map { addContactCenter(company, it) }
                .map { alertNewMessageToAgents(it).subscribe() }
                .flatMap { whatsChatRepository.save(whatsChat) }
        }
    }

    private fun prepareContactToSave(remoteJid: String, company: Long, instanceId: Int): Mono<Contact> {
        val profilePicture = getProfilePicture(instanceId, remoteJid)
        if(profilePicture.picture !== null){
            //println("IMAGEM DO PERFIL: ${profilePicture.picture}")
            return contactRepository.save(Contact(0, "Desconhecido", remoteJid, company, instanceId, profilePicture.picture))
        }
        println("CAGOU AO PEGAR FOTO DO PERFIL ${profilePicture.errorMessage}")
        return contactRepository.save(Contact(0, "Desconhecido", remoteJid, company, instanceId, null))
    }

    @GetMapping("/{remoteJid}")
    fun chats(@PathVariable remoteJid: String, @RequestParam("limit") limit: Int): Flux<WhatsChat> {
        return whatsChatRepository.findTop50ByRemoteJidOrderByDatetimeDesc(remoteJid)
    }
}