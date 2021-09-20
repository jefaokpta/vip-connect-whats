package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.repository.GreetingRepository
import br.com.vipsolutions.connect.service.GreetingService
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
@RestController
@RequestMapping("/api/robot/greeting")
class GreetingController(
    private val greetingRepository: GreetingRepository,
    private val greetingService: GreetingService
) {

    @GetMapping
    fun getAll() = greetingRepository.findAll()

    @PostMapping
    fun save(@RequestBody greeting: Greeting) = greetingRepository.findByControlNumber(greeting.controlNumber)
        .doFirst { println("POST GREETING $greeting") }
        .flatMap { greetingRepository.save(Greeting(greeting, it)) }
        .switchIfEmpty(greetingService.setCompanyIdAndSave(greeting))

    @DeleteMapping("/{controlNumber}")
    fun delete(@PathVariable controlNumber: Long) = greetingRepository.deleteByControlNumber(controlNumber)
}