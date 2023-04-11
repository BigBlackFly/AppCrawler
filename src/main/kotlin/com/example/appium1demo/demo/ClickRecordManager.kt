package com.example.appium1demo.demo

//data class ElementStatusModule(
//    val element: AppCrawler3.ElementModule,
//    var isClicked: Boolean
//)

data class PageClickStatusModule(
    val page: AppCrawler.PageModule,
    val clickedElements: MutableList<AppCrawler.ElementModule>
)

object ClickRecordManager {

    // record the clicked status of every element. the clicked info will be useful when traversal.
    private val mPagesClickStatusList: MutableList<PageClickStatusModule> = mutableListOf()

    /**
     * record that this element was clicked.
     */
    fun recordElementClicked(page: AppCrawler.PageModule, element: AppCrawler.ElementModule) {
        val recordedPage = mPagesClickStatusList.find { it.page == page }
        if (recordedPage != null) {
            // update the existing PageClickStatusModule
            recordedPage.clickedElements.add(element)
        } else {
            // add new PageClickStatusModule
            mPagesClickStatusList.add(
                PageClickStatusModule(
                    page = page,
                    clickedElements = mutableListOf(element)
                )
            )
        }
    }

    fun getIsClicked(page: AppCrawler.PageModule, element: AppCrawler.ElementModule): Boolean {
        return mPagesClickStatusList.find { it.page == page }?.clickedElements?.contains(element) == true
    }

}