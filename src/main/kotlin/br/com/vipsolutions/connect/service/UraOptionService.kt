package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.model.robot.UraOption
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.UraOptionRepository
import br.com.vipsolutions.connect.repository.UraRepository
import org.springframework.stereotype.Service

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
@Service
class UraOptionService(
    private val uraOptionRepository: UraOptionRepository,
    private val companyRepository: CompanyRepository,
    private val uraRepository: UraRepository
) {

    fun fillOptions(ura: Ura) = uraOptionRepository.findAllByUraId(ura.id)
        .collectList()
        .map { ura.apply { options = it } }

    fun saveUraWithCompany(ura: Ura) = companyRepository.findByControlNumber(ura.controlNumber)
        .flatMap { uraRepository.save(ura.apply { company = it.id }) }

    fun setUraIdInOptions(options: List<UraOption>, uraId: Long) = options
        .apply { map { it.uraId = uraId } }

    fun updateUraOptions(ura: Ura) = uraOptionRepository.deleteAllByUraId(ura.id)
        .then(uraOptionRepository.saveAll(setUraIdInOptions(ura.options, ura.id)).collectList())

    fun deleteUraAndUraOptions(ura: Ura) = uraRepository.delete(ura)
        .then(uraOptionRepository.deleteAllByUraId(ura.id))
}