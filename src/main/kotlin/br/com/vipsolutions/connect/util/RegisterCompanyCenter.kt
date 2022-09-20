package br.com.vipsolutions.connect.util

import org.springframework.web.reactive.socket.WebSocketSession

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-05-13
 */

class RegisterCompanyCenter {
    companion object{
        val companies = mutableMapOf<Long, WebSocketSession>()
        private val registerTries = mutableMapOf<Long, Int>()

        fun removeTries(companyId: Long){
            if(registerTries.containsKey(companyId)){
                registerTries.remove(companyId)
            }
        }
        fun plusRegisterTries(companyId: Long): Int {
            if(registerTries.containsKey(companyId)){
                registerTries[companyId] = registerTries[companyId]!! + 1
            }else{
                registerTries[companyId] = 1
            }
            return registerTries[companyId]!!
        }
    }

}