package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.repository.CompanyRepository
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
@RequestMapping("/api/companies")
class CompanyController(private val companyRepository: CompanyRepository) {

    @GetMapping
    fun getAll() = companyRepository.findAll()
        .filter { !it.deleted }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = companyRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody company: Company) = companyRepository.save(company)
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}

    @PutMapping
    fun update(@RequestBody company: Company) = companyRepository.findById(company.id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))
        .flatMap { companyRepository.save(Company(it, company)) }
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}
        .flatMap { Mono.empty<Void>() }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = companyRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada.")))
        .flatMap{companyRepository.save(it.apply { deleted = true })}
        .flatMap{Mono.empty<Void>()}
}