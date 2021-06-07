package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.util.ContactCenter
import br.com.vipsolutions.connect.websocket.SessionCentral
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@RestController
@RequestMapping("/api/contacts")
class ContactController(private val contactRepository: ContactRepository) {

    @GetMapping("/memory/company/{company}")
    fun listMemoryContacts(@PathVariable company: Long) = Mono.justOrEmpty(ContactCenter.contacts[company])
        .map { it.values.toList() }
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "lista não encontrado.")))

    @GetMapping("/memory/agent/{company}")
    fun listMemoryAgentSessions(@PathVariable company: Long) = Mono.justOrEmpty(SessionCentral.agents[company])
        .map { it.keys.toList() }
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "lista não encontrado.")))

//    @GetMapping
//    fun getAll() = contactRepository.findAll()
//
//    @GetMapping("/{id}")
//    fun getOne(@PathVariable id: Long) = contactRepository.findById(id)
//        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    fun save(@RequestBody contact: Contact) = contactRepository.save(contact)
//        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}
//
//    @PutMapping
//    fun update(@RequestBody contact: Contact) = contactRepository.findById(contact.id)
//        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))
//        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}
//        .flatMap { Mono.empty<Void>() }
//
//    @DeleteMapping("/{id}")
//    fun delete(@PathVariable id: Long) = contactRepository.findById(id)
//        .flatMap { contactRepository.deleteById(id) }
//        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))
}