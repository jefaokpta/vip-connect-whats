package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.repository.UraOptionRepository
import br.com.vipsolutions.connect.repository.UraRepository
import br.com.vipsolutions.connect.service.UraOptionService
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
@RestController
@RequestMapping("/api/robot/ura")
class UraController(
    private val uraRepository: UraRepository,
    private val uraOptionRepository: UraOptionRepository,
    private val uraOptionService: UraOptionService
) {

    @GetMapping
    fun getAll() = uraRepository.findAll()
        .flatMap { uraOptionService.fillOptions(it) }

    @PostMapping
    fun save(@RequestBody ura: Ura) = uraRepository.findByCompany(ura.company)
        .flatMap { uraRepository.save(Ura(ura, it)) }
        .doOnNext{uraOptionRepository.deleteAllByUraId(ura.id)}
        .switchIfEmpty(uraRepository.save(ura))
        .map { uraOptionRepository.saveAll(ura.options) }
}