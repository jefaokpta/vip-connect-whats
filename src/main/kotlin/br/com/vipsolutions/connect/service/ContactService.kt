package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ContactDAO
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

    fun createContact(contactDAO: ContactDAO) = companyRepository.findByControlNumber(contactDAO.controlNumber)
        .flatMap { company ->
            contactRepository.save(Contact(contactDAO.apply {
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
        .log()

        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}

    private fun addProfilePicture(contact: Contact) = Mono.justOrEmpty(getProfilePicture(contact.instanceId, contact.whatsapp).picture)
        .flatMap { contactRepository.save(contact.apply { imgUrl = it }) }

}