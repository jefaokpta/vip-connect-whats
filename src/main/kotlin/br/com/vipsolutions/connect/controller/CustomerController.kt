package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.Customer
import br.com.vipsolutions.connect.repository.CustomerRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-07
 */
@RestController
@RequestMapping("/api/customers")
class CustomerController(private val customerRepository: CustomerRepository) {

    @GetMapping
    fun getAll() = customerRepository.findAll()
        .filter { !it.deleted }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = customerRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.")))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody customer: Customer) = customerRepository.save(customer)
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}

    @PutMapping
    fun update(@RequestBody customer: Customer) = customerRepository.findById(customer.id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.")))
        .flatMap { customerRepository.save(customer.apply { updatedAt = LocalDateTime.now() }) }
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}
        .flatMap { Mono.empty<Void>() }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = customerRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.")))
        .flatMap{customerRepository.save(it.apply { deleted = true })}
        .flatMap{ Mono.empty<Void>()}
}