package br.com.vipsolutions.connect.websocket

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SessionCentralKtTest{

    //@Test
    fun interrompeLoopTeste(){
        val mapa = mutableMapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5)
        val mapa2 = HashMap(mapa)

        println(mapa)
        println(mapa2)

        mapa2.forEach{ num ->
            if(num.key == 3){
                mapa.remove(3)
            }
        }

        println(mapa)
        println(mapa2)
    }
}