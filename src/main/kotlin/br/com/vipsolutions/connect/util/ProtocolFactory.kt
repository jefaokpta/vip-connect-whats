package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.model.Contact

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 14/10/21
 */

fun generateProtocol(contact: Contact) = contact.apply { protocol = System.currentTimeMillis() }