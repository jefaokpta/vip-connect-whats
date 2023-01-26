package br.com.vipsolutions.connect.service

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class ContactServiceTest {

    @Test
    fun clearWhatsappNumber() {
        val whatsNumber = "55 11 9-9999-9999"

        val whatsNumberCleaned = whatsNumber.replace(" ", "").replace("-", "")

        assertEquals("5511999999999", whatsNumberCleaned)
    }
}