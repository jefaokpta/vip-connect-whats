package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.sendQuizButtonsMessage
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ContactAndQuiz
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.QuizRepository
import br.com.vipsolutions.connect.repository.UraRepository
import br.com.vipsolutions.connect.util.AnsweringQuizCenter
import br.com.vipsolutions.connect.websocket.SessionCentral
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 10/09/21
 */
@Service
class WsChatHandlerService(
    private val contactRepository: ContactRepository,
    private val uraRepository: UraRepository,
    private val quizRepository: QuizRepository,
) {

    fun sendQuizOrFinalizeMsg(contact: Contact): Mono<Contact> = quizRepository.existsByCompany(contact.company)
        .flatMap { hasQuiz ->
            if (hasQuiz){
                val contactCopy = contact.copy()
                resetContact(contact)
                    .flatMap { quizRepository.findByCompany(it.company) }
                    .doOnNext { AnsweringQuizCenter.quizzes[contact.whatsapp] = ContactAndQuiz(contactCopy, it) }
                    .map { sendQuizButtonsMessage(contact, it); contact }
            }else{
                resetContact(contact)
                    .doFinally {finalizeAttendance(contact).subscribe()}
            }
        }

    private fun resetContact(contact: Contact): Mono<Contact> {
        contact.category = null
        contact.protocol = null
        contact.isNewProtocol = false
        return contactRepository.save(contact)
    }

    fun finalizeAttendance(contact: Contact) = uraRepository.findTop1ByCompanyAndActive(contact.company)
        .map { Optional.ofNullable(it.finalMessage) }
        .map { if (it.isPresent) sendTextMessage(contact.whatsapp, it.get(), contact.instanceId) }

    fun contactsFilteredByLastCategory(company: Company, agent: Int): Mono<MutableList<Contact>> {
        val agentCategories = SessionCentral.getAllByCompanyId(company.id)?.get(agent)?.categories ?: return Mono.empty()
        return contactRepository.findTop300ByCompanyOrderByLastMessageTimeDesc(company.id)
            .filter { agentCategories.contains(it.lastCategory) }
            .collectList()
    }

}

