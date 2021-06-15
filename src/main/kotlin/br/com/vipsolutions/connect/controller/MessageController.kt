package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ws.AgentActionWs
import br.com.vipsolutions.connect.model.ws.MessageCount
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import br.com.vipsolutions.connect.util.ContactCenter
import br.com.vipsolutions.connect.util.objectToJson
import br.com.vipsolutions.connect.websocket.SessionCentral
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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

        val datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("-03:00"))
        //println(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date.from(datetime.toInstant(ZoneOffset.of("-03:00")))))

        jsonObject.addProperty("error", "Erro: Não encontrado texto ou conversa.")
        val text = jsonObject.getAsJsonObject("message")["conversation"]?:
            jsonObject.getAsJsonObject("message")["text"]?: jsonObject["error"]

        val whatsChat = WhatsChat(messageId, remoteJid, text.asString, fromMe, status, datetime)
        return if(fromMe){
            whatsChatRepository.findById(messageId)
                .flatMap { dbWhatsChat ->
                    dbWhatsChat.status = whatsChat.status
                    dbWhatsChat.isPersistable = false
                    whatsChatRepository.save(dbWhatsChat)
                }
                .switchIfEmpty(whatsChatRepository.save(whatsChat))

        }
        else {
            contactRepository.findByWhatsapp(remoteJid)
                .switchIfEmpty(contactRepository.save(Contact(0, "Desconhecido", remoteJid, company)))
                .map { addContactCenter(company, it) }
                .map ( this::alertNewMessageToAgents )
                .flatMap { whatsChatRepository.save(whatsChat) }
        }
    }

    private fun alertNewMessageToAgents(contact: Contact) = Optional.ofNullable(SessionCentral.agents[contact.company])
        .map { Flux.fromIterable(it.values) }
        .orElse(Flux.empty())
        .flatMap {it.send(Mono.just(it.textMessage(objectToJson(AgentActionWs("NEW_MESSAGE", 0, 0, null, contact))))) }
        .subscribe()

    private fun addContactCenter(company: Long, contact: Contact): Contact {
        if(ContactCenter.contacts.contains(company)){
            if(ContactCenter.contacts[company]!!.contains(contact.id)){
               val messageCount = ContactCenter.contacts[company]!![contact.id]!!
               messageCount.message = messageCount.message + 1
               contact.newMessageQtde = messageCount.message
               contact.newMessage = true
            }
            else{
                contact.newMessageQtde = 1
                contact.newMessage = true
                ContactCenter.contacts[company]!![contact.id] = MessageCount(contact.id)
            }
        }
        else{
            ContactCenter.contacts[company] = mutableMapOf(contact.id to MessageCount(contact.id))
            contact.newMessageQtde = 1
            contact.newMessage = true
        }
        return contact
    }

    @GetMapping("/{remoteJid}")
    fun chats(@PathVariable remoteJid: String, @RequestParam("limit") limit: Int): Flux<WhatsChat> {
        return whatsChatRepository.findTop50ByRemoteJidOrderByDatetimeDesc(remoteJid)
    }
}