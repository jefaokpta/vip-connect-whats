package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.robot.Ura
import br.com.vipsolutions.connect.model.robot.UraAnswer

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 03/09/21
 */
class AnsweringUraCenter {
    companion object{
        private val uraAnswers = mutableMapOf<String, UraAnswer>()

        fun addUraAnswer(contact: Contact, ura: Ura){
            uraAnswers["${contact.company}-${contact.whatsapp}"] = UraAnswer(ura)
        }

        fun getUraAnswer(contact: Contact): UraAnswer{
            return uraAnswers["${contact.company}-${contact.whatsapp}"]!!
        }

        fun removeUraAnswer(contact: Contact){
            uraAnswers.remove("${contact.company}-${contact.whatsapp}")
        }

        fun containsUraAnswer(contact: Contact): Boolean{
            return uraAnswers.containsKey("${contact.company}-${contact.whatsapp}")
        }

        fun getUraAnswerCounter(contact: Contact): Int{
            return uraAnswers["${contact.company}-${contact.whatsapp}"]!!.tryAgain
        }

        fun plusUraAnswerCounter(contact: Contact){
            uraAnswers["${contact.company}-${contact.whatsapp}"] = uraAnswers["${contact.company}-${contact.whatsapp}"]!!.apply { tryAgain++ }
        }
    }
}