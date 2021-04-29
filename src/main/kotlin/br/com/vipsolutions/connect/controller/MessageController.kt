package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val whatsChatRepository: WhatsChatRepository
) {

    @PostMapping("/test")
    fun sendingTextMessageToNode(@RequestBody json: String){
        sendTextMessage(json, 3001)
    }
    @PostMapping @Transactional
    fun received(@RequestBody payload: String): Mono<WhatsChat> {
        println(payload)
        val node = jacksonObjectMapper().readValue(payload, ObjectNode::class.java)
        val remoteJid = node["key"]["remoteJid"].textValue()
        val messageId = node["key"]["id"].textValue()
        val fromMe = node["key"]["fromMe"].booleanValue()
        val status = node["status"].intValue()
        val text = Optional.ofNullable(node.findValue("conversation"))
            .orElse(node.findValue("text"))


        val whatsChat = WhatsChat(messageId, remoteJid, text.asText(), fromMe, status)
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
            whatsChatRepository.save(whatsChat)
        }
    }

    @GetMapping("/{remoteJid}")
    fun chats(@PathVariable remoteJid: String, @RequestParam("limit") limit: Int): Flux<WhatsChat> {
        return whatsChatRepository.findTop50ByRemoteJidOrderByDatetimeDesc(remoteJid)
    }
}