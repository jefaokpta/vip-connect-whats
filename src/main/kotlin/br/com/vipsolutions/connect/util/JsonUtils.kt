package br.com.vipsolutions.connect.util

import com.google.gson.Gson

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-05-13
 */

fun objectToJson(obj: Any) = Gson().toJson(obj)