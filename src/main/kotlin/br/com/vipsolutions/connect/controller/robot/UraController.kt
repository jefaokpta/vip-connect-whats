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
    fun save(@RequestBody ura: Ura) = uraOptionService.saveUraWithCompany(ura)
        .doFirst { println("CREATE URA $ura"); println("CREATE URA OPTIONS ${ura.options}") }
        .flatMap { uraOptionRepository.saveAll(uraOptionService.setUraIdInOptions(ura.options, it.id)).collectList() }

    @PutMapping
    fun update(@RequestBody ura: Ura) = uraRepository.findByControlNumberAndVipUraId(ura.controlNumber, ura.vipUraId)
        .doFirst { println("UPDATE URA $ura"); println("UPDATE URA OPTIONS ${ura.options}") }
        .flatMap { uraRepository.save(Ura(ura, it)) }
        .flatMap { uraOptionService.updateUraOptions(it)}

    @DeleteMapping("/{controlNumber}/{vipUraId}")
    fun delete(@PathVariable controlNumber: Long, @PathVariable vipUraId: Long) = uraRepository.findByControlNumberAndVipUraId(controlNumber, vipUraId)
        .flatMap { uraOptionService.deleteUraAndUraOptions(it) }
}