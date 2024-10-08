package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.dto.ContactDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 28/10/22
 */
internal class ContactTest{

        @Test
        fun `Contact lastMessageTime nao pode ser NULL`(){
            val contact = Contact(ContactDTO("Jefferson", "5511987654321", 100200))
            assertNotNull(contact.lastMessageTime)
        }
}