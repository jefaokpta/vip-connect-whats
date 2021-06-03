package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.model.Contact

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-02
 */
class ContactCenter {
    companion object{
        val contacts = mutableMapOf<Long, MutableMap<Long, Contact>>()
    }
}