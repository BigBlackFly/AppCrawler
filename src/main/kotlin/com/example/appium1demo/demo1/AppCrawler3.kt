package com.example.appium1demo.demo1

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
import java.util.logging.Logger

class AppCrawler3 {

    companion object {
        const val TAG = "AppCrawler"

        @JvmStatic
        fun main(args: Array<String>) {
            val appCrawler = AppCrawler3()
            appCrawler.crawApp()
        }
    }

    data class PageModule(
        // 经过特殊处理的xml
        val pageSource: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (other !is PageModule) {
                return false
            }
            return other.pageSource == this.pageSource
        }

        override fun hashCode(): Int {
            return pageSource.hashCode()
        }
    }

    data class ElementModule(
        val resId: String = "",
        val pkgName: String = "",
        val className: String = "",
        val index: String = ""
    ) {
        fun getWebElement(driver: AndroidDriver): WebElement {
            if (resId.isNotEmpty()) {
                return driver.findElements(By.id(resId))[0]
            }
            return driver.findElement(By.xpath("")) // TODO: xpath
        }
    }

    data class StepModule(
        val page: PageModule,
        val element: ElementModule
    )

    private val mLogger = Logger.getLogger(TAG)

    // nav trace
    private val mTraceStack = Stack<StepModule>()

    private lateinit var mDriver: AndroidDriver

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

    private fun getElements(): List<ElementModule> {
        val webElements = getWebElements()
        val elements = mutableListOf<ElementModule>()

        webElements.forEach {
            elements.add(
                ElementModule(
                    resId = it.getAttribute("resource-id") ?: "",
                    pkgName = it.getAttribute("package") ?: "",
                    className = it.getAttribute("class") ?: "",
                    index = it.getAttribute("index") ?: ""
                )
            )
        }
        return elements
    }

    private fun craw() {
        val page = getCurrentPage()
        val elements = getElements()


        // STEP1: traversal all the elements
        elements.forEach {
            if (!ClickRecordManager.getIsClicked(page, it)) {

                val webElement = it.getWebElement(mDriver)
                webElement.click()
                ClickRecordManager.recordElementClicked(page, it)

                if (getCurrentPage() != page) { // nav
                    // push the nav step into nav stack
                    mTraceStack.push(StepModule(page, it))

                    if (!isLooped()) { // to new page
                        craw()
                    } else { // to a previous page
                        // back trace, to recover from the previous page
                        performBackTrace()
                    }

                } else { // no nav
                    // do nothing
                }
            }
        }


        // STEP2: press back key
        performBack()
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

    /**
     * whether the app jumped to a previously visited page
     */
    private fun isLooped(): Boolean {
        val currentPage = getCurrentPage()
        // whether page is appeared before
        return mTraceStack.find { it.page == currentPage } != null
    }

    private fun getCurrentPage(): PageModule {
        return PageModule(pageSource = getPageSource())
    }

    private fun getPageSource(): String {
        val raw = mDriver.pageSource
        return refinePageSource(raw)
    }

    private fun refinePageSource(pageSource: String): String {
        return pageSource
    }

    private fun getWebElements(): List<WebElement> {
        // note: if an element is set with an OnClickListener, it is absolutely clickable.
        return mDriver.findElements(By.xpath("//*[@clickable=\"true\"]"))
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
}