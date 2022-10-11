package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.ContactDAO
import br.com.vipsolutions.connect.service.ContactService
import br.com.vipsolutions.connect.util.ContactCenter
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
class ContactController(private val contactService: ContactService) {

    @GetMapping("/memory/company/{company}")
    fun listMemoryContacts(@PathVariable company: Long) = Mono.justOrEmpty(ContactCenter.findOne(company))
        .map { it.values.toList() }
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "lista não encontrado.")))

    @PostMapping @CrossOrigin
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody contactDAO: ContactDAO) = contactService.createContact(contactDAO)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "EMPRESA NÃO ENCONTRADA.")))



//    @GetMapping
//    fun getAll() = contactRepository.findAll()
//
//    @GetMapping("/{id}")
//    fun getOne(@PathVariable id: Long) = contactRepository.findById(id)
//        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))
//
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