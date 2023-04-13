package com.example.appium1demo.demo.data.config

import com.example.appium1demo.demo.utils.JsonUtil
import org.junit.Assume
import org.junit.Test
import java.io.File
import java.io.FileReader

class JsonUtilTest {

    @Test
    fun test() {
        val file = File("src/main/kotlin/com/example/appium1demo/demo/data/config/config.json")
        Assume.assumeTrue(file.exists())

        val reader = FileReader(file)
        val chars = CharArray(file.length().toInt())
        reader.read(chars)
        val json = String(chars)
        println("json = $json")
        Assume.assumeTrue(json.isNotEmpty())

        val config = JsonUtil.fromJson(json, Config::class.java)
        println("config = $config")
    }

}