package br.com.vipsolutions.connect.util

import br.com.vipsolutions.connect.model.EnvironmentVar
import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 18/08/21
 */


fun loadVars(): EnvironmentVar {
    println("CARREGANDO VARIAVEIS DO ARQUIVO env.json")
    val filePath = Path.of("env.json")
    if (Files.exists(filePath)){
        val readString = Files.readString(filePath)
        val env = Gson().fromJson(readString, EnvironmentVar::class.java)
        if (env.nodeWhatsVersion.isNullOrBlank() || env.uploadedFileFolder.isNullOrBlank()){
            println("REVEJA env.json ESTA COM ERROS!!")
            exitProcess(1)
        }
        println(env)
        return env
    }
    else{
        println("FALTANDO ARQUIVO env.json COM VARIAVEIS DE AMBIENTE!!")
        exitProcess(1)
    }
}