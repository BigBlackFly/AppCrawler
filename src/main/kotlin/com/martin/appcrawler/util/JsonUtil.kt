package com.martin.appcrawler.util

import com.google.gson.Gson
import javax.annotation.Nullable

object JsonUtil {

    private val mGson = Gson()

    @Nullable
    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return mGson.fromJson(json, clazz)
    }

    fun toJson(obj: Any): String {
        return mGson.toJson(obj)
    }
}