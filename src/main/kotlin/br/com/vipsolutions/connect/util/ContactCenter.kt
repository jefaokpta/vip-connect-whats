package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.ContactsAndId
import br.com.vipsolutions.connect.model.ws.MessageCount

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-02
 */
class ContactCenter {
    companion object{
        val contacts = mutableMapOf<Long, MutableMap<Long, MessageCount>>()
    }

}

fun addContactCenter(company: Long, contact: Contact): Contact {
    if (!contact.busy){
        if(ContactCenter.contacts.contains(company)){
            if(ContactCenter.contacts[company]!!.contains(contact.id)){
                val messageCount = ContactCenter.contacts[company]!![contact.id]!!
                messageCount.message = messageCount.message + 1
                contact.newMessageQtde = messageCount.message
                contact.newMessage = true
            }
            else{
                contact.newMessageQtde = 1
                contact.newMessage = true
                ContactCenter.contacts[company]!![contact.id] = MessageCount(contact.id)
            }
        }
        else{
            ContactCenter.contacts[company] = mutableMapOf(contact.id to MessageCount(contact.id))
            contact.newMessageQtde = 1
            contact.newMessage = true
        }
    }
    return contact
}

fun contactsHaveNewMessages(contacts: List<Contact>): ContactsAndId {
    val contactsAndId = ContactsAndId(contacts, contacts[0].company)
    val contactMap = ContactCenter.contacts[contactsAndId.companyId] ?: return contactsAndId
    contacts.forEach{ contact ->
        if (contactMap.containsKey(contact.id)) {
            contact.newMessage = true
            contact.newMessageQtde = contactMap[contact.id]!!.message
        }
    }
    return contactsAndId
}
