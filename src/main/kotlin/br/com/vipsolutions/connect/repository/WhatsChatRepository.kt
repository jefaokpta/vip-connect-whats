package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.WhatsChat
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-12
 */
interface WhatsChatRepository: ReactiveCrudRepository<WhatsChat, String> {

    fun findTop500ByRemoteJidAndCompanyOrderByDatetimeDesc(remoteJid: String, company: Long): Flux<WhatsChat>

    fun findTop500ByRemoteJidAndCompanyAndCategoryOrderByDatetimeDesc(remoteJid: String, company: Long, category: Long): Flux<WhatsChat>

    fun findAllByProtocolOrderByDatetimeDesc(protocol: Long): Flux<WhatsChat>

    fun findByMessageIdAndCompany(messageId: String, company: Long): Mono<WhatsChat>
}