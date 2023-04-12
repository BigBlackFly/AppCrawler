package com.example.appium1demo.demo.data

import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/**
 * an item represents a appium element.
 */
data class Item(
    val resId: String = "",
    val pkgName: String = "",
    val className: String = ""
) {
    fun getWebElement(driver: AndroidDriver): WebElement {
        if (resId.isNotEmpty()) {
            return driver.findElements(By.id(resId))[0]
        }
        return driver.findElement(By.xpath("")) // TODO: xpath
    }
}