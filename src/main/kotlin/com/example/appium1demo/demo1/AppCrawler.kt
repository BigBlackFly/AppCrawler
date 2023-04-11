package com.example.appium1demo.demo1

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebElement
import java.net.URL
import java.util.*

class AppCrawler {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val appCrawler = AppCrawler()
            appCrawler.crawApp()
        }
    }

    data class Node(
        // 是否是虚节点
        var isVirtual: Boolean = true,
        // 是否爬取完成。对于虚节点，true代表其名下的子节点全部为true。对于实节点，true即表示其被爬取完成。
        var isCrawled: Boolean = false,
        // 节点的本体物理元素
        var self: WebElement? = null,
        // 如果为null说明是叶子节点，点击不会调转，或是空页面的虚根节点
        // 如果为空列表说明尚不知道该信息
        // 如果不为null说明是指向其他节点的指针，虚节点只会指向一个或多个实节点，实节点只会指向一个单一的虚节点
        var nodes: List<Node>? = null
    )

    private lateinit var driver: AndroidDriver

    private fun initAppium() {
        val appiumServerUrl = URL("http://127.0.0.1:4723/wd/hub")
        val capabilities = DesiredCapabilities().apply {
            setCapability("appium:" + MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2")
            setCapability("appium:" + MobileCapabilityType.TIMEOUTS, 3600)
            setCapability("appium:" + MobileCapabilityType.PLATFORM_NAME, "Android")
            setCapability("appium:" + MobileCapabilityType.PLATFORM_VERSION, "13")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_PACKAGE, "com.appcrawler.target")
            setCapability("appium:" + AndroidMobileCapabilityType.APP_ACTIVITY, "com.appcrawler.target.MainActivity")
        }
        driver = AndroidDriver(appiumServerUrl, capabilities)
    }

    /**
     * the only entrance of program
     */
    fun crawApp() {
        // start Launcher Page
        initAppium()
        val appRootNode = getNodeOfCurrentPage()
        craw(appRootNode)
    }

    private fun craw(node: Node?) {
        if (node == null) return

        val page = getCurrentPage()

        if (node.isVirtual) {
            // traversal all the children nodes and back to parent page
            node.nodes?.forEach {
                if (!it.isCrawled) craw(it)
            }
            // after all the children nodes of virtual node are crawled, set virtual node isCrawled = true
            node.isCrawled = true
            performBack()
        } else {
            if (node.isCrawled) return
            performClick(node)
            if (getCurrentPage() != page) { // navigation! clicked node is another parent node.
                if (isInOldPage(page)) {
                    // TODO: make a judge: if nav to previous page, need to play again the nav trace, and continue to traverse that page.
                } else {
                    val virtualNode = getNodeOfCurrentPage()
                    // update the info of [node]. it contains only one node, the virtual node of next page. we're now building the tree while traversal.
                    node.nodes = listOf(virtualNode)
                    // craw the new page (recursively)
                    craw(virtualNode)
                }
            } else { // no navigation. clicked node is leaf node.
                node.nodes = null
                node.isCrawled = true
            }
        }
    }

    private fun getCurrentPage(): String {
        // for now, we only check activities. TODO: we need check dialogs, fragments, etc.
        return driver.currentActivity()
    }

    private fun isInOldPage(page: String): Boolean {
        // TODO:
        return false
    }

    private fun getNodeOfCurrentPage(): Node {
        val elements = getElementsInPage()
        // get the leaf nodes of current page
        val leafNodes = mutableListOf<Node>()
        elements.forEach {
            leafNodes.add(
                Node(
                    isVirtual = false,
                    self = it,
                    nodes = listOf()
                )
            )
        }
        // get the virtual root node of new page
        val virtualNode = Node(isVirtual = true, self = null, nodes = leafNodes)
        return virtualNode
    }

    private fun getElementsInPage(): List<WebElement> {
        // note: if an element is set with an OnClickListener, it is absolutely clickable.
        return driver.findElements(By.xpath("//*[@clickable=\"true\"]"))
    }

    private fun performClick(node: Node) {
        node.self?.click() // TODO: save log, screenshot
        // if swipe take effects, then no need to click. otherwise we will try to click. Because there's usually one way to interact with an element.
        println("clicked element id ${(node.self as RemoteWebElement).id}")
    }

    private fun performBack() {
        driver.pressKey(KeyEvent(AndroidKey.BACK))
    }

    private fun <T> Stack<T>.popUpper(item: T) {
        val itemDepth = this.search(item)
        if (itemDepth <= 1) return
        for (i in 1 until itemDepth) {
            pop()
        }
    }
}