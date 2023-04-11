package com.example.appium1demo.test

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.remote.DesiredCapabilities
import java.net.URL


class AppiumTest {

    private lateinit var driver: AndroidDriver

    @Before
    fun setup() {
        val appiumServerUrl = URL("http://127.0.0.1:4723/wd/hub")
        val capabilities = DesiredCapabilities().apply {
            setCapability("appium:" + MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_NAME, "Android")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_VERSION, "13")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_PACKAGE, "com.zui.camera")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_ACTIVITY, "com.zui.camera.CameraLauncher")
        }
        driver = AndroidDriver(appiumServerUrl, capabilities)
        // give the user some time to handle with the popped permission requesting dialogs, before we really run tests cases.
        Thread.sleep(5000L)
    }

    @Test
    fun test() {
        val imageButtons = driver.findElements(By.className("android.widget.ImageButton"))
        println("imageButtons = $imageButtons")
    }

    @Test
    fun test2() {
        val imageButtons = driver.findElements(By.xpath("//android.widget.ImageView[@content-desc=\"快门\"]"))
        println("imageButtons = $imageButtons")
    }


}