package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.model.robot.UraOption
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.UraOptionRepository
import br.com.vipsolutions.connect.repository.UraRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

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

    @Transactional
    fun createUra(ura: Ura) = saveUraWithCompany(ura)
        .doFirst { println("CREATE URA $ura"); println("CREATE URA OPTIONS ${ura.options}") }
        .flatMap (this::verifyUraActive)
        .flatMap { uraOptionRepository.saveAll(setUraIdInOptions(ura.options, it.id)).collectList() }

    @Transactional
    fun updateUra(ura: Ura) = uraRepository.findByControlNumberAndVipUraId(ura.controlNumber, ura.vipUraId)
        .doFirst { println("UPDATE URA $ura"); println("UPDATE URA OPTIONS ${ura.options}") }
        .flatMap { uraRepository.save(Ura(ura, it)) }
        .flatMap { verifyUraActive(it) }
        .flatMap { updateUraOptions(it)}

    @Transactional
    fun deleteUra(controlNumber: Long, vipUraId: Long)= uraRepository.findByControlNumberAndVipUraId(controlNumber, vipUraId)
        .flatMap { deleteUraAndUraOptions(it) }

    fun fillOptions(ura: Ura) = uraOptionRepository.findAllByUraId(ura.id)
        .collectList()
        .map { ura.apply { options = it } }

    private fun saveUraWithCompany(ura: Ura) = companyRepository.findByControlNumber(ura.controlNumber)
        .flatMap { uraRepository.save(ura.apply { company = it.id }) }

    private fun setUraIdInOptions(options: List<UraOption>, uraId: Long) = options
        .apply { map { it.uraId = uraId } }

    private fun verifyUraActive(ura: Ura): Mono<Ura> {
        if (ura.active) {
            return uraRepository.findAllByControlNumber(ura.controlNumber)
                .filter { it.id != ura.id }
                .flatMap { uraRepository.save(it.copy(active = false)) }
                .then(Mono.just(ura))
        }
        return Mono.just(ura)
    }

    private fun updateUraOptions(ura: Ura) = uraOptionRepository.deleteAllByUraId(ura.id)
        .then(uraOptionRepository.saveAll(setUraIdInOptions(ura.options, ura.id)).collectList())

    private fun deleteUraAndUraOptions(ura: Ura) = uraRepository.delete(ura)
        .then(uraOptionRepository.deleteAllByUraId(ura.id))


}