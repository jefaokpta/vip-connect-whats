package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.AuthWhatsapp
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-17
 */
interface AuthWhatsappRepository: ReactiveCrudRepository<AuthWhatsapp, Long> {

    fun findByCompanyId(company: Long): Mono<AuthWhatsapp>

    fun deleteByCompanyId(companyId: Long): Mono<Void>
}