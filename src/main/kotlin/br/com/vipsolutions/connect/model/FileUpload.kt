package br.com.vipsolutions.connect.model

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 17/08/21
 */
class FileUpload(
    val remoteJid: String,
    val instanceId: Int,
    val caption: String,
    val ptt: Boolean = false,
    var filePath: String?
) {
}