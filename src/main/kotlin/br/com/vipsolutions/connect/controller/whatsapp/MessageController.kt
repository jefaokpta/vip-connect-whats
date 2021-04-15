package br.com.vipsolutions.connect.controller.whatsapp

import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.repository.WhatsChatRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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
@RequestMapping("/whats/messages")
class MessageController(
    private val whatsChatRepository: WhatsChatRepository
) {

    @PostMapping @Transactional
    fun received(@RequestBody payload: String): Mono<WhatsChat> {
        println(payload)
        val node = ObjectMapper().readValue(payload, ObjectNode::class.java)
        val remoteJid = node["key"]["remoteJid"].textValue()
        val messageId = node["key"]["id"].textValue()
        val fromMe = node["key"]["fromMe"].booleanValue()
        val status = node["status"].intValue()
        val text = Optional.ofNullable(node.findValue("conversation"))
            .orElse(node.findValue("text"))
        //println(node.findValue("conversation"))


        val whatsChat = WhatsChat(0, remoteJid, text.asText(), messageId, fromMe, status)
        return if(fromMe){
            whatsChatRepository.findByMessageId(messageId)
                .flatMap { dbWhatsChat ->
                    dbWhatsChat.status = whatsChat.status
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
        println(limit)
        return whatsChatRepository.findTop50ByRemoteJid(remoteJid)
    }
}