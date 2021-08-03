package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.repository.ContactRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 03/08/21
 */
@Component
class CronTask(private val contactRepository: ContactRepository) {

    @Scheduled(cron = "5 2 4 * * *")
    private fun testCron(){
        ProfilePicture(contactRepository).update().subscribe()
    }
}