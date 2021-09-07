package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.Company
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-27
 */
interface CompanyRepository: ReactiveCrudRepository<Company, Long> {

    fun findByControlNumber(controlNumber: Long): Mono<Company>

    @Query("SELECT MAX(instance) FROM companies ")
    fun max(): Mono<Int>
}