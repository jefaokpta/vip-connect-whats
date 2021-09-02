package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.repository.UraOptionRepository
import org.springframework.stereotype.Service

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
@Service
class UraOptionService(private val uraOptionRepository: UraOptionRepository) {

    fun fillOptions(ura: Ura) = uraOptionRepository.findAllByUraId(ura.id)
        .collectList()
        .map { ura.apply { options = it } }
}