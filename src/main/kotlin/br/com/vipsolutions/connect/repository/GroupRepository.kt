package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.Group
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */
interface GroupRepository: ReactiveCrudRepository<Group, Long> {

    fun findAllByControlNumber(controlNumber: Long): Flux<Group>
}