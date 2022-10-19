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
        private val contacts = mutableMapOf<Long, MutableMap<Long, MessageCount>>()

        fun findOne(id: Long): MutableMap<Long, MessageCount>? {
            return contacts[id]
        }

        fun listAllNewMessages() = contacts.values.flatMap { it.values }

        fun removeForced(contactId: Long) = contacts.values.forEach { it.remove(contactId) }

        fun remove(companyId: Long, contactId: Long) {
            contacts[companyId]?.remove(contactId)
        }
        fun addContactCenter(company: Long, contact: Contact): Contact {
            if (!contact.busy){
                if(contacts.contains(company)){
                    if(contacts[company]!!.contains(contact.id)){
                        val messageCount = contacts[company]!![contact.id]!!
                        messageCount.message = messageCount.message + 1
                        contact.newMessageQtde = messageCount.message
                        contact.newMessage = true
                    }
                    else{
                        contact.newMessageQtde = 1
                        contact.newMessage = true
                        contacts[company]!![contact.id] = MessageCount(contact.id)
                    }
                }
                else{
                    contacts[company] = mutableMapOf(contact.id to MessageCount(contact.id))
                    contact.newMessageQtde = 1
                    contact.newMessage = true
                }
            }
            return contact
        }
        fun contactsHaveNewMessages(contactList: List<Contact>): ContactsAndId {
            if (contactList.isEmpty()){
                return ContactsAndId(contactList, 0)
            }
            val contactsAndId = ContactsAndId(contactList, contactList[0].company)
            val contactMap = contacts[contactsAndId.companyId] ?: return contactsAndId
            contactList.forEach{ contact ->
                if (contactMap.containsKey(contact.id)) {
                    contact.newMessage = true
                    contact.newMessageQtde = contactMap[contact.id]!!.message
                }
            }
            return contactsAndId
        }
    }
}
