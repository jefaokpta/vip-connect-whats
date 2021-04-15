package br.com.vipsolutions.connect.websocket

import org.springframework.web.reactive.socket.WebSocketSession

class SessionCentral {

    companion object {
        val sessions = mutableMapOf<String, WebSocketSession>()
    }
}