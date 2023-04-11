package com.example.appium1demo.demo

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.DesiredCapabilities
import java.net.URL
import java.util.*

class AppCrawler {

    companion object {
        const val TAG = "AppCrawler"

        @JvmStatic
        fun main(args: Array<String>) {
            val appCrawler = AppCrawler()
            appCrawler.crawApp()
        }
    }

    // nav trace
    private val mTraceStack = Stack<StepModule>()

    private lateinit var mDriver: AndroidDriver

    private val mParser = XmlParser()

    private fun initAppium() {
        val appiumServerUrl = URL("http://127.0.0.1:4723/wd/hub")
        val capabilities = DesiredCapabilities().apply {
            setCapability("appium:" + MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2")
            setCapability("appium:" + MobileCapabilityType.TIMEOUTS, "3600")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_NAME, "Android")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_VERSION, "13")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_PACKAGE, "com.appcrawler.target")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_ACTIVITY, "com.appcrawler.target.MainActivity")
        }
        mDriver = AndroidDriver(appiumServerUrl, capabilities)
    }

    /**
     * the only entrance of program
     */
    fun crawApp() {
        // start Launcher Page
        initAppium()
        craw()
    }

    private fun craw() {
        val page = getCurrentPage()
        val elements = getElements()
        log("page = $page")
        log("elements = $elements")

        // STEP1: traversal all the elements in page
        elements.forEach {
            log("is ever clicked before = ${ClickRecordManager.getIsClicked(page, it)}")

            if (!ClickRecordManager.getIsClicked(page, it)) {

                val webElement = it.getWebElement(mDriver)
                webElement.click()
                log("click!")
                ClickRecordManager.recordElementClicked(page, it)

                log("isNaved = ${getCurrentPage() != page}")
                if (getCurrentPage() != page) { // nav
                    // push the nav step into nav stack
                    mTraceStack.push(StepModule(page, it))

                    log("isLooped = ${isLooped()}")
                    if (!isLooped()) { // to new page
                        craw()
                    } else { // to a previous page
                        // back trace, to recover from the previous page
                        log("will back trace...")
                        performBackTrace()
                    }

                } else { // no nav
                    // do nothing
                }
            }
        }


        // STEP2: press back key
        performBack()
        log("back!")
    }

    private fun getCurrentPage(): PageModule {
        return PageModule(pageSource = getPageSource())
    }

    private fun getElements(): List<ElementModule> {
        val webElements = getWebElements()
        val elements = mutableListOf<ElementModule>()

        webElements.forEach {
            elements.add(
                ElementModule(
                    resId = it.getAttribute("resource-id") ?: "",
                    pkgName = it.getAttribute("package") ?: "",
                    className = it.getAttribute("class") ?: ""
                )
            )
        }
        return elements
    }

    /**
     * whether the app jumped to a previously visited page
     */
    private fun isLooped(): Boolean {
        val currentPage = getCurrentPage()
        // whether page is appeared before
        return mTraceStack.find { it.page == currentPage } != null
    }

    private fun getPageSource(): String {
        val raw = mDriver.pageSource
        return mParser.refine(mDriver, raw)
    }

    private fun getWebElements(): List<WebElement> {
        // note: if an element is set with an OnClickListener, it is absolutely clickable.
        return mDriver.findElements(By.xpath("//*[@clickable=\"true\"]"))
    }

    /**
     * back trace, step by step.
     *
     * when the app navigated to a previously appeared page, call this method to nav back.
     */
    private fun performBackTrace() {
        val fromPage = getCurrentPage()
        val fromStep = mTraceStack.findLast { it.page == fromPage }
        val fromIndex = mTraceStack.indexOf(fromStep)
        // destination: the top of the stack
        val toIndex = mTraceStack.size - 1
        for (index in fromIndex until toIndex) {
            val step = mTraceStack[index]
            val webElement = step.element.getWebElement(mDriver)
            webElement.click()
            mTraceStack.push(step)
        }
    }

    private fun performBack() {
        mDriver.pressKey(KeyEvent(AndroidKey.BACK))
    }

    private fun <T> Stack<T>.popUpper(item: T) {
        val itemDepth = this.search(item)
        if (itemDepth <= 1) return
        for (i in 1 until itemDepth) {
            pop()
        }
    }

    private fun log(message: String) {
        println()
        println(message)
    }
}