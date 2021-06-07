package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.Contact

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-03
 */
class AgentActionWs(
    val action: String,
    val agent: Int,
    val company: Int,
    var contacts: List<Contact>?
) {
}