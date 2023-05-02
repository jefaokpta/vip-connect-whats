package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.service.VoiceLeadService
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 02/05/23
 */
@RestController
@RequestMapping("/api/voice-lead")
class VoiceLeadController( private val voiceLeadService: VoiceLeadService) {
    @PostMapping("/{controlNumber}")
    fun sendVoiceLead(@PathVariable controlNumber: Long, @RequestBody payload: String) =
        voiceLeadService.sendVoiceLead(controlNumber, payload)
}