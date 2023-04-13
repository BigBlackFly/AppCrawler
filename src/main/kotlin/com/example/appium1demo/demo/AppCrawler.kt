package com.example.appium1demo.demo

import com.example.appium1demo.demo.data.Item
import com.example.appium1demo.demo.data.Page
import com.example.appium1demo.demo.data.Step
import com.example.appium1demo.demo.data.StepAction
import com.example.appium1demo.demo.utils.XmlParser
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

    // navigation trace
    private val mTraceStack = Stack<Step>()

    private lateinit var mDriver: AndroidDriver

    private val mParser = XmlParser()

    private fun initAppium() {
        val appiumServerUrl = URL("http://127.0.0.1:4723/wd/hub")
        val capabilities = DesiredCapabilities().apply {
            setCapability("appium:" + MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2")
            setCapability("appium:" + MobileCapabilityType.TIMEOUTS, "3600")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_NAME, "Android")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_VERSION, "13")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_PACKAGE, "com.zui.calculator")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_ACTIVITY, "com.zui.calculator.Calculator")
        }
        mDriver = AndroidDriver(appiumServerUrl, capabilities)
    }

    private fun quitAppium() {
        mDriver.quit()
    }

    /**
     * the only entrance of program
     */
    fun crawApp() {
        // start Launcher Page
        initAppium()
        crawl()
        quitAppium()
    }

    private fun crawl() {
        Thread.sleep(2000L)
        log("\n\n****************************************craw!****************************************")
        val page = getCurrentPage()
        val items = getCurrentItems(page.activity)
        log("page = ${page.md5()}")
        log("items = $items")

        // STEP1: traversal all the items in page
        items.forEachIndexed { index, item ->
            log("item[$index] has been ever clicked = ${ClickRecorder.getIsClicked(page, item)}")

            if (ClickRecorder.getIsClicked(page, item)) {
                return@forEachIndexed
            }

            click(item)
            log("clicked item[$index]!")
            ClickRecorder.recordClicked(page, item)

            val currentPage = getCurrentPage()
            log("current page = ${currentPage.md5()}")
            log("isNaved = ${currentPage != page}")
            if (currentPage != page) { // nav
                // push the nav step into nav stack
                mTraceStack.push(Step(page, item, StepAction.CLICK))

                log("isLooped = ${isLooped()}")
                if (!isLooped()) { // now at a new page
                    // craw the new page
                    crawl()
                } else { // now at a previous page
                    // back trace, to recover from the previous page
                    log("will back trace to ${page.md5()}...")
                    performBackTrace()
                }

            } else { // no nav
                // do nothing
            }

        }


        // STEP2: press back key
        back()
        mTraceStack.push(Step(page, null, StepAction.BACK))
        log("back!")
    }

    private fun getCurrentPage(): Page {
        return Page(
            activity = getActivity(),
            pageSource = getPageSource()
        )
    }

    private fun getCurrentItems(currentActivity: String): List<Item> {
        val webElements = getWebElements()
        val items = mutableListOf<Item>()

        webElements.forEach {
            items.add(
                Item(
                    resId = it.getAttribute("resource-id") ?: "",
                    pkgName = it.getAttribute("package") ?: "",
                    className = it.getAttribute("class") ?: ""
                )
            )
        }
        // drop those items which should not be clicked.
        // TODO: read blackList from configuration file
        when (currentActivity) {
            ".CameraLauncher" -> {
                items.remove(
                    Item(
                        resId = "android:id/button2",
                        pkgName = "com.zui.camera",
                        className = "android.widget.Button"
                    )
                )
            }
        }
        return items
    }

    /**
     * whether the app jumped to a previously visited page
     */
    private fun isLooped(): Boolean {
        val currentPage = getCurrentPage()
        // whether page is appeared before
        return mTraceStack.find { it.page == currentPage } != null
    }

    private fun getActivity(): String {
        return mDriver.currentActivity()
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
     * when the app navigated to a previously appeared page, call this method to nav back to the latest page.
     */
    private fun performBackTrace() {
        val fromPage = getCurrentPage()
        val fromStep = mTraceStack.findLast { it.page == fromPage }
        val fromIndex = mTraceStack.indexOf(fromStep)
        // destination: the top of the stack
        val toIndex = mTraceStack.size - 1
        for (index in fromIndex until toIndex) {
            val step = mTraceStack[index]
            step.action(mDriver)
            mTraceStack.push(step)
        }
    }

    private fun click(item: Item) {
        item.getWebElement(mDriver).click()
    }

    private fun back() {
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