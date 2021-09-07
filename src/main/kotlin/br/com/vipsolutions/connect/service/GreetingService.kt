package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.GreetingRepository
import org.springframework.stereotype.Service

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 07/09/21
 */
@Service
class GreetingService(private val companyRepository: CompanyRepository, private val greetingRepository: GreetingRepository) {

    fun setCompanyIdAndSave(greeting: Greeting) = companyRepository.findByControlNumber(greeting.controlNumber)
        .map { greeting.apply { company = it.id } }
        .flatMap (greetingRepository::save)
}