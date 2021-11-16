package br.com.vipsolutions.connect.controller.chat

import br.com.vipsolutions.connect.repository.WhatsChatRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 16/11/21
 */
@RestController
@RequestMapping("/api/chats")
class ChatController(private val whatsChatRepository: WhatsChatRepository) {

    @GetMapping("/{protocol}")
    fun getChatByProtocol(@PathVariable protocol: Long) = whatsChatRepository.findAllByProtocolOrderByDatetimeDesc(protocol)

}