package br.com.vipsolutions.connect.service

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.repository.ContactRepository
import br.com.vipsolutions.connect.websocket.SessionCentral
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 10/09/21
 */
@Service
class WsChatHandlerService(private val contactRepository: ContactRepository) {

    fun contactsFilteredByLastCategory(company: Company, agent: Int): Mono<MutableList<Contact>> {
        val agentCategorys = SessionCentral.agents[company.id]?.get(agent)?.categories ?: return Mono.empty()
        return contactRepository.findAllByCompanyOrderByLastMessageTimeDesc(company.id)
            .filter { agentCategorys.contains(it.lastCategory) }
            .collectList()
    }
}