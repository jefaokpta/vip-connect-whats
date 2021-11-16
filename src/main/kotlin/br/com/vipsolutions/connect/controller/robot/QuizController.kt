package br.com.vipsolutions.connect.controller.robot

import br.com.vipsolutions.connect.model.robot.Quiz
import br.com.vipsolutions.connect.repository.CompanyRepository
import br.com.vipsolutions.connect.repository.QuizRepository
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 04/11/21
 */
@RestController
@RequestMapping("/api/robot/quiz")
class QuizController(
    private val quizRepository: QuizRepository,
    private val companyRepository: CompanyRepository
) {

    @GetMapping
    fun getAll() = quizRepository.findAll()

    @PostMapping
    fun save(@RequestBody quiz: Quiz) = quizRepository.findByControlNumber(quiz.controlNumber)
        .doFirst { println("POST QUIZ $quiz") }
        .flatMap { quizRepository.save(Quiz(quiz, it)) }
        .switchIfEmpty(setCompanyIdAndSave(quiz))

    @DeleteMapping("/{controlNumber}")
    fun delete(@PathVariable controlNumber: Long) = quizRepository.deleteByControlNumber(controlNumber)

    private fun setCompanyIdAndSave(quiz: Quiz) = companyRepository.findByControlNumber(quiz.controlNumber)
        .map { quiz.apply { company = it.id } }
        .flatMap(quizRepository::save)
}