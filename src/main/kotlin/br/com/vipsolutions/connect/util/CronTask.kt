package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.client.sendQuizAnswerToVip
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.service.WsChatHandlerService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 03/08/21
 */
    private const val  SECOND: Long = 1000
    private const val MINUTE = SECOND * 60
    private const val HOUR = MINUTE * 60
@Component
class CronTask(
    private val contactRepository: ContactRepository,
    private val wsChatHandlerService: WsChatHandlerService
) {

    // sec min hour day month weekday
    @Scheduled(cron = "5 2 4 * * *")
    private fun updateContactProfilePicture(){
        ProfilePicture(contactRepository).update().subscribe()
    }

    @Scheduled(fixedDelay = MINUTE * 3)
    private fun answerQuizTimeout(){
        println("APAGANDO ATRASADOS ${LocalDateTime.now()}")
        val quizzes = AnsweringQuizCenter.quizzes.toMap()
        quizzes.values.forEach {
            println("TESTANDO ${it.contact}")
            println("SOMA TA DANDO QUANTO? ${it.dateTime.plusMinutes(5)}")
            if (it.dateTime.plusMinutes(5).isBefore(LocalDateTime.now())){
                println("APAGAREI ${it.contact.whatsapp}")
                AnsweringQuizCenter.quizzes.remove(it.contact.whatsapp)
                sendQuizAnswerToVip(it, 0)
                wsChatHandlerService.finalizeAttendance(it.contact).subscribe()
            }
        }
    }
}