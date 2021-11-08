package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.robot.Quiz
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 04/11/21
 */
interface QuizRepository: ReactiveCrudRepository<Quiz, Long> {

    fun deleteByControlNumber(controlNumber: Long): Mono<Void>

    fun findByControlNumber(controlNumber: Long): Mono<Quiz>
    fun findByCompany(company: Long): Mono<Quiz>

    fun existsByCompany(company: Long): Mono<Boolean>

}