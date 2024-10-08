package br.com.vipsolutions.connect.model

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 17/08/21
 */
data class FileUpload(
    val remoteJid: String?,
    val instanceId: Int?,
    val fileType: String,
    val ptt: Boolean = false,
    val caption: String?,
    var filePath: String?,
    val messageGroupId: Long?
) {
}