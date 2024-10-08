package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.model.Company
import br.com.vipsolutions.connect.model.CompanyInfo

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-28
 */

fun createInstance(company: Company): CompanyInfo {
    val command = "docker run -d --name=whats-${company.id} -p${company.instance}:3000 -e COMPANY=${company.id} -e API_PORT=${company.instance} --restart=on-failure -v /opt/whatsMediaHost:/whatsMedia jefaokpta/node-whats:${EnvironmentVarCenter.environmentVar.nodeWhatsVersion}"
    return Runtime.getRuntime().exec(command).inputStream.bufferedReader().lines().findFirst()
        .map{CompanyInfo(company, it)}
        .orElse(CompanyInfo(company, "Container Whats já estava UP"))
}

fun stopInstance(company: Company): CompanyInfo {
    val command = "docker stop whats-${company.id}"
    return Runtime.getRuntime().exec(command).inputStream.bufferedReader().lines().findFirst()
        .map{CompanyInfo(company, it)}
        .orElse(CompanyInfo(company, "Sei la oq pode ter dado ruim no stop"))
}

fun startInstance(company: Company): CompanyInfo {
    val command = "docker start whats-${company.id}"
    return Runtime.getRuntime().exec(command).inputStream.bufferedReader().lines().findFirst()
        .map{CompanyInfo(company, it)}
        .orElse(CompanyInfo(company, "Sei la oq pode ter dado ruim no start"))
}

fun destroyInstance(company: Company): CompanyInfo {
    val command = "docker rm -f whats-${company.id}"
    println("DESTRUINDO INSTANCIA whats-${company.id}")
    return Runtime.getRuntime().exec(command).inputStream.bufferedReader().lines().findFirst()
        .map{CompanyInfo(company, it)}
        .orElse(CompanyInfo(company, "Container Inexistente"))
}

fun removeAuthFile(companyId: Long) {
    val command = "rm -f /opt/whatsMediaHost/auths/auth_info_multi-${companyId}.json"
    Runtime.getRuntime().exec(command)
}