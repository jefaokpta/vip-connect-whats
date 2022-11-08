package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.dao.ContactDAO
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

    @GetMapping("/memory/new-messages")
    fun listNewMessagesCount() = ContactCenter.listAllNewMessages()

    @DeleteMapping("/memory/new-messages/{id}")
    fun removeMessagesCount(@PathVariable id: Long) = ContactCenter.removeForced(id)

    @GetMapping("/{controlNumber}")
    fun listAll(@PathVariable controlNumber: Long) = contactService.listAll(controlNumber)

    @GetMapping("/blocked/{controlNumber}")
    fun listAllBlocked(@PathVariable controlNumber: Long) = contactService.listAllBlocked(controlNumber)

    @PostMapping("/block/{id}")
    fun block(@PathVariable id: Long) = contactService.blockUnblock(id, "block")

    @PostMapping("/unblock/{id}")
    fun unblock(@PathVariable id: Long) = contactService.blockUnblock(id, "unblock")
}