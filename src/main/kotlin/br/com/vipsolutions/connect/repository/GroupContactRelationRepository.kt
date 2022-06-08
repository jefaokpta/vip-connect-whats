package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.relation.GroupContactRelation
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */
interface GroupContactRelationRepository: ReactiveCrudRepository<GroupContactRelation, Long> {

    fun findAllByGroupId(id: Long): Flux<GroupContactRelation>
    fun deleteAllByGroupId(id: Long): Mono<Void>
}