package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.WhatsChat
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-12
 */
interface WhatsChatRepository: ReactiveCrudRepository<WhatsChat, Long> {

    fun findTop50ByRemoteJid(remoteJid: String): Flux<WhatsChat>
    fun findByMessageId(messageId: String): Mono<WhatsChat>

}