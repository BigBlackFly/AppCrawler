package com.example.appium1demo.demo

import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

data class ElementModule(
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