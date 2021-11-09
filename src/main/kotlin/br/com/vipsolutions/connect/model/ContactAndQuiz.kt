package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.robot.Quiz
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 08/11/21
 */
class ContactAndQuiz(
    val contact: Contact,
    val quiz: Quiz,
    val dateTime: LocalDateTime = LocalDateTime.now()
) {
}