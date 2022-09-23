package br.com.vipsolutions.connect.controller.robot

import br.com.vipsolutions.connect.model.robot.Ura
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
    private val uraOptionService: UraOptionService
) {

    @GetMapping
    fun getAll() = uraRepository.findAll()
        .flatMap { uraOptionService.fillOptions(it) }

    @PostMapping
    fun save(@RequestBody ura: Ura) = uraOptionService.createUra(ura)

    @PutMapping
    fun update(@RequestBody ura: Ura) = uraOptionService.updateUra(ura)

    @DeleteMapping("/{controlNumber}/{vipUraId}")
    fun delete(@PathVariable controlNumber: Long, @PathVariable vipUraId: Long) = uraOptionService.deleteUra(controlNumber, vipUraId)
}