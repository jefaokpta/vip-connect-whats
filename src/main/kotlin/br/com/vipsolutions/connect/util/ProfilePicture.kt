package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.client.getProfilePicture
import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.repository.ContactRepository
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 03/08/21
 */
class ProfilePicture(private val contactRepository: ContactRepository) {

    fun update() = contactRepository.findAll()
        .map(::updatePP)
        .flatMap(contactRepository::save)

    private fun updatePP(contact: Contact): Contact {
        println("TENTANDO ATUALIZAR FOTO DE ${contact.whatsapp} ${LocalDateTime.now()}")
        // todo: ignorar se contato tiver espaco ou traco no atributo whatsapp (erro de cadastro)
        contact.imgUrl = getProfilePicture(contact.instanceId, contact.whatsapp).picture?: return contact
        println("CONSEGUI ${contact.whatsapp}")
        return contact
    }
}