package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.service.CompanyService
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-27
 */
@RestController
@RequestMapping("/api/companies")
class CompanyController(
    private val companyService: CompanyService,
    private val companyRepository: CompanyRepository
) {

//    @GetMapping
//    fun list() = companyRepository.findAll()
//
//    @PostMapping
//    fun newCompany(@RequestBody company: Int) = companyService.createCompany(company)
//
//    @GetMapping("/stop/{id}")
//    fun stop(@PathVariable id: Long) = companyService.stopCompany(id)
//
//    @GetMapping("/start/{id}")
//    fun start(@PathVariable id: Long) = companyService.startCompany(id)
//
//    @GetMapping("/destroy/{id}")
//    fun destroy(@PathVariable id: Long) = companyService.destroyCompany(id)

}