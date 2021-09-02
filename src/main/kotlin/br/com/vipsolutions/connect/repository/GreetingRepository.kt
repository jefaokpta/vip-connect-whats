package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.robot.Greeting
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
interface GreetingRepository: ReactiveCrudRepository<Greeting, Long> {

    fun findByCompany(company: Long): Mono<Greeting>
}