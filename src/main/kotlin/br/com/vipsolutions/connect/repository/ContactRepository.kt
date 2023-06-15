package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.Contact
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
interface ContactRepository: ReactiveCrudRepository<Contact, Long> {

    fun findByWhatsappAndCompany(whatsapp: String, company: Long): Mono<Contact>

    fun findTop300ByCompanyOrderByLastMessageTimeDesc(company: Long): Flux<Contact>

    fun findAllByCompanyOrderByNameAsc(company: Long): Flux<Contact>

    fun findAllByCompanyAndIsBlockedOrderByNameAsc(company: Long, blocked: Boolean = true): Flux<Contact>
}