package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.Contact
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
interface ContactRepository: ReactiveCrudRepository<Contact, Long> {

    fun findByWhatsapp(whatsapp: String): Mono<Contact>
}