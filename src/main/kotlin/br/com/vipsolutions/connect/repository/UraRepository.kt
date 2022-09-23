package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.robot.Ura
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
interface UraRepository: ReactiveCrudRepository<Ura, Long> {

    fun findTop1ByCompanyAndActive(company: Long, active: Boolean = true): Mono<Ura>

    fun findByCompanyAndVipUraId(company: Long, vipUraId: Long): Mono<Ura>

    fun findByControlNumberAndVipUraId(controlNumber: Long, vipUraId: Long): Mono<Ura>

    fun findAllByControlNumber(controlNumber: Long): Flux<Ura>
}