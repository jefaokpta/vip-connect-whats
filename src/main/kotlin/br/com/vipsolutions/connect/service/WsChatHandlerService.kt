package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.client.sendQuizButtonsMessage
import br.com.vipsolutions.connect.client.sendTextMessage
import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.repository.QuizRepository
import br.com.vipsolutions.connect.repository.UraOptionRepository
import br.com.vipsolutions.connect.repository.UraRepository
import br.com.vipsolutions.connect.websocket.SessionCentral
import kotlinx.coroutines.reactive.collect
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
    private val uraOptionRepository: UraOptionRepository,
    private val quizRepository: QuizRepository,
) {

    fun sendQuizOrFinalizeMsg(contact: Contact) = quizRepository.existsByCompany(contact.company)
        .flatMap { hasQuiz ->
            if (hasQuiz){
                quizRepository.findByCompany(contact.company)
                    .map { sendQuizButtonsMessage(contact, it) }
            }else{
                uraRepository.findByCompany(contact.company)
                    .map { Optional.ofNullable(it.finalMessage) }
                    .map { if (it.isPresent) sendTextMessage(contact.whatsapp, it.get(), contact.instanceId) }
            }
        }

    fun contactsFilteredByLastCategory(company: Company, agent: Int): Mono<MutableList<Contact>> {
        val agentCategorys = SessionCentral.agents[company.id]?.get(agent)?.categories ?: return Mono.empty()
        return contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(company.id)
            .filter { agentCategorys.contains(it.lastCategory) }
            .collectList()
    }

    fun sendTransferMessage(contact: Contact) = uraRepository.findByCompany(contact.company)
        .map { uraOptionRepository.findAllByUraId(it.id) }
        .flatMap { it.collectList() }
        .map { options ->
            options.forEach { option ->
                if (option.departmentId == contact.category){
                    sendTextMessage(contact.whatsapp, "Você está no Departamento: ${option.department}", contact.instanceId)
                }
            }
        }
}

