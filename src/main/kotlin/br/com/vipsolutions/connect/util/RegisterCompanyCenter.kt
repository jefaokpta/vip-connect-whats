package br.com.vipsolutions.connect.util

import org.springframework.web.reactive.socket.WebSocketSession

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-05-13
 */
class RegisterCompanyCenter {
    companion object{
        val companies = mutableMapOf<Long, WebSocketSession>()
    }
}