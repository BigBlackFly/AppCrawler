package com.martin.appcrawler.core

import com.martin.appcrawler.data.Item
import com.martin.appcrawler.data.Page
import com.martin.appcrawler.data.Step
import com.martin.appcrawler.data.StepAction
import com.martin.appcrawler.util.XmlParser
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
    private val mFinishedPages = mutableListOf<Page>()
    private lateinit var mDriver: AndroidDriver

    private fun initAppium() {
        val appiumServerUrl = URL("http://127.0.0.1:4723")
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
        log("\n\n****************************************craw!****************************************")
        val page = getCurrentPage()
        val items = getConfiguredItemsIn(page)
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
                if (currentPage in mFinishedPages) {
                    log("current page has been finished")
                    back()
                    return@forEachIndexed
                }
                val isLooped = isLooped()
                log("isLooped = $isLooped")
                if (!isLooped) { // now at a new page
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
        mFinishedPages.add(page)


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

    /**
     * @return the items need test in page, ruled by configuration file.
     */
    private fun getConfiguredItemsIn(page: Page): List<Item> {
        val webElements = getWebElements()
        val items = mutableListOf<Item>()

        // TODO: select items according to configuration file
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
        when (page.activity) {
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
        return XmlParser.refine(mDriver, raw)
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