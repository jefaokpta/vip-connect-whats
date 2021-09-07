package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.robot.Ura
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
interface UraRepository: ReactiveCrudRepository<Ura, Long> {

    fun findByCompany(company: Long): Mono<Ura>
    fun findByControlNumber(controlNumber: Long): Mono<Ura>
}