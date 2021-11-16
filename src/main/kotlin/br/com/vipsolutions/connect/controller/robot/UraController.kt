package br.com.vipsolutions.connect.controller.robot

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
    fun save(@RequestBody ura: Ura) = uraRepository.findByControlNumber(ura.controlNumber)
        .doFirst { println("POST URA $ura") }
        .flatMap { uraRepository.save(Ura(ura, it)) }
        .switchIfEmpty(uraOptionService.saveUraWithCompany(ura))
        .doOnNext{uraOptionRepository.deleteAllByUraId(it.id).subscribe()}
        .flatMap { uraOptionRepository.saveAll(uraOptionService.setUraIdInOptions(ura.options, it.id)).collectList() }
        .then()
//        .log()

    @DeleteMapping("/{controlNumber}")
    fun delete(@PathVariable controlNumber: Long) = uraRepository.findByControlNumber(controlNumber)
        .doOnNext{uraOptionRepository.deleteAllByUraId(it.id).subscribe()}
        .flatMap { uraRepository.deleteById(it.id) }
}