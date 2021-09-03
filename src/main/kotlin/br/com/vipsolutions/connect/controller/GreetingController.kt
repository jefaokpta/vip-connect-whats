package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.repository.GreetingRepository
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/09/21
 */
@RestController
@RequestMapping("/api/robot/greeting")
class GreetingController(private val greetingRepository: GreetingRepository) {

    @GetMapping
    fun getAll() = greetingRepository.findAll()

    @PostMapping
    fun save(@RequestBody greeting: Greeting) = greetingRepository.findByCompany(greeting.company)
        .flatMap { greetingRepository.save(Greeting(greeting, it)) }
        .switchIfEmpty(greetingRepository.save(greeting))
}