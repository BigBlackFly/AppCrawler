package com.example.appium1demo.demo.utils

import io.appium.java_client.android.AndroidDriver
import org.dom4j.DocumentException
import org.dom4j.io.SAXReader
import utils.XmlTools
import java.io.ByteArrayInputStream
import java.io.IOException

object XmlParser {
    private val reader = SAXReader()
    private val attributes = listOf("package", "class", "resource-id")

    @Throws(IOException::class)
    fun refine(driver: AndroidDriver, pageSource: String): String {
        try {
            ByteArrayInputStream(pageSource.toByteArray()).use { targetStream ->
                Thread.sleep(1000)
                val currentDocument = reader.read(targetStream)
                val refinedPageSource = XmlTools.filterDocument(currentDocument, attributes)
                val pageName = driver.currentActivity()
                return """
                    $pageName
                    $refinedPageSource
                    """.trimIndent()
            }
        } catch (e: DocumentException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }
}