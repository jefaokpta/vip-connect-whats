package br.com.vipsolutions.connect.model.robot

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 27/08/21
 */
@Table("quiz")
data class Quiz(
    @Id
    var id: Long,
    var company: Long,
    val controlNumber: Long,
    val question: String,
    val btnFooterText: String?,
) {
    constructor(quiz: Quiz, dbQuiz: Quiz): this(
        dbQuiz.id,
        dbQuiz.company,
        dbQuiz.controlNumber,
        quiz.question,
        quiz.btnFooterText,
    )
}