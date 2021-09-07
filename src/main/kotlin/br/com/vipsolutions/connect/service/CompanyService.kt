package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.util.createInstance
import br.com.vipsolutions.connect.util.destroyInstance
import br.com.vipsolutions.connect.util.startInstance
import br.com.vipsolutions.connect.util.stopInstance
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.switchIfEmpty

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-29
 */
@Service
class CompanyService(private val companyRepository: CompanyRepository) {

    fun infoCompany(controlNumber: Long) = companyRepository.findByControlNumber(controlNumber)

    fun createCompany(controlNumber: Long) = verifyControlNumberExists(controlNumber)
        .flatMap (this::activateInstance)
        //.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))

    fun stopCompany(id: Long) = companyRepository.findById(id)
        .flatMap { companyRepository.save(it.apply { isStopped = true }) }
        .map(::stopInstance)
        //.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))

    fun startCompany(id: Long) = companyRepository.findById(id)
        .flatMap { companyRepository.save(it.apply { isStopped = false }) }
        .map(::startInstance)
        //.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))

    fun destroyCompany(id: Long) = companyRepository.findById(id)
        .flatMap { companyRepository.save(it.apply { isActive = false }) }
        .map(::destroyInstance)
        //.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))


    private fun activateInstance(company: Company) =
        companyRepository.save(company.apply { isActive = true; isStopped = false })
        .map ( ::createInstance )

    private fun verifyControlNumberExists(controlNumber: Long) = companyRepository.findByControlNumber(controlNumber)
            .switchIfEmpty { companyRepository.count()
                    .flatMap { count ->
                        if (count > 0) {
                            companyRepository.max().flatMap { companyRepository.save(Company(0, controlNumber, it + 1)) }
                        } else {
                            companyRepository.save(Company(0, controlNumber, 3001))
                        }
                    }
            }



}