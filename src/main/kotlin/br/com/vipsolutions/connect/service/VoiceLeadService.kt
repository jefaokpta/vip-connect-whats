package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.ContactRepository
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/05/23
 */
@Service
class VoiceLeadService(
    private val contactRepository: ContactRepository,
    private val companyRepository: CompanyRepository
) {

    // payload example: "personal_phone":"+55 (11) 96170-1301",
    @Transactional
    fun sendVoiceLead(controlNumber: Long, payload: String): Mono<Unit> {
        println("INFO: VOICE LEAD PAYLOAD $payload")
        val leads = jacksonObjectMapper().readValue(payload, ObjectNode::class.java)
        val phone = leads["leads"][0]["personal_phone"].asText()
            .filter { it.isDigit() }
            .plus("@s.whatsapp.net")
        val name = leads["leads"][0]["name"].asText()
        println("INFO: VOICE LEAD PHONE $phone NAME $name")
        return companyRepository.findByControlNumber(controlNumber)
            .flatMap { company ->
                contactRepository.findByWhatsappAndCompany(phone, company.id)
                    .switchIfEmpty(contactRepository.save(Contact(
                        id = 0,
                        name = name,
                        whatsapp = phone,
                        company = company.id,
                        instanceId = company.instance,
                        lastCategory = 0
                    ))
                )
            }.map { sendTextMessage(it.whatsapp, "oooopa", it.instanceId) }
    }
}
