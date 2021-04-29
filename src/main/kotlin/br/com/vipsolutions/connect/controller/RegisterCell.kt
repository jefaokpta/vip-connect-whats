package br.com.vipsolutions.connect.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-23
 */
@RestController
@RequestMapping("/api/register")
class RegisterCell {

    @PostMapping
    fun receiveQrCode(@RequestBody payload: String){
        println(payload)
        val qrcode = ObjectMapper().readValue(payload, ObjectNode::class.java).findValue("qrCode")
        println(qrcode.textValue())
    }
}