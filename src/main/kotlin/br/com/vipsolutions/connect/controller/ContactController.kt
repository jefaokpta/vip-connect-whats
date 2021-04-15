package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.Customer
import br.com.vipsolutions.connect.repository.ContactRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
@RestController
@RequestMapping("/api/contacts")
class ContactController(private val contactRepository: ContactRepository) {

    @GetMapping
    fun getAll() = contactRepository.findAll()

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = contactRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody contact: Contact) = contactRepository.save(contact)
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}

    @PutMapping
    fun update(@RequestBody contact: Contact) = contactRepository.findById(contact.id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))
        .flatMap { contactRepository.save(contact.apply { updatedAt = LocalDateTime.now() }) }
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}
        .flatMap { Mono.empty<Void>() }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = contactRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.")))
        .flatMap{contactRepository.save(it.apply { deleted = true })}
        .flatMap{ Mono.empty<Void>()}
}