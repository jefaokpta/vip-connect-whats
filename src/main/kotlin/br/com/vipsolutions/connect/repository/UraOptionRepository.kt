package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.robot.UraOption
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
interface UraOptionRepository: ReactiveCrudRepository<UraOption, Long> {

    fun findAllByUraId(uraId: Long): Flux<UraOption>
}