package br.com.vipsolutions.connect.model.ws

import br.com.vipsolutions.connect.model.CompanyInfo

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-05-10
 */
class ActionWs(
    val action: String,
    val controlNumber: Long,
    val instanceId: Long,
    val qrCode: QrCode?,
    val companyInfo: CompanyInfo?
) {
}