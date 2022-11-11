package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.blockUnblockContact
import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.dto.ContactDTO
import br.com.vipsolutions.connect.model.dto.ContactLite
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.ContactRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 04/04/22
 */
@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val companyRepository: CompanyRepository
) {

    fun createContact(contactDTO: ContactDTO) = companyRepository.findByControlNumber(contactDTO.controlNumber)
        .flatMap { company ->
            contactRepository.save(Contact(contactDTO.apply {
                whatsapp = whatsapp.plus("@s.whatsapp.net")
                controlNumber = company.id
                instanceId = company.instance
            }))
        }
        .doOnNext {contact ->
            Mono.just("CRIANDO EXECUCAO TARDIA")
                .flatMap { addProfilePicture(contact) }
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe()
            println("Contact ${contact.name} created")
        }
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}

    private fun addProfilePicture(contact: Contact) = Mono.justOrEmpty(getProfilePicture(contact.instanceId, contact.whatsapp).picture)
        .flatMap { contactRepository.save(contact.apply { imgUrl = it }) }

    fun listAll(controlNumber: Long) = companyRepository.findByControlNumber(controlNumber)
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada")))
        .flatMapMany { contactRepository.findAllByCompanyOrderByNameAsc(it.id) }
        .map(::ContactLite)

    fun listAllBlocked(controlNumber: Long) = companyRepository.findByControlNumber(controlNumber)
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada")))
        .flatMapMany { contactRepository.findAllByCompanyAndIsBlockedOrderByNameAsc(it.id) }
        .map(::ContactLite)

    fun blockUnblock(id: Long, action: String)  = contactRepository.findById(id)
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado")))
        .flatMap { blockUnblockContact(it, action) }
        .flatMap { contactRepository.save(it.copy(isBlocked = action == "block")) }
        .then()

}