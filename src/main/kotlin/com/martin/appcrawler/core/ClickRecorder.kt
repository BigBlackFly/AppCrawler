package com.martin.appcrawler.core

import com.martin.appcrawler.data.Item
import com.martin.appcrawler.data.Page

object ClickRecorder {

    data class PageClickStatusModule(
            val page: Page,
            val clickedItems: MutableList<Item>
    )

    // record the clicked status of every item. the clicked info will be useful when traversal.
    private val mPagesClickStatusList: MutableList<PageClickStatusModule> = mutableListOf()

    /**
     * record that this item was clicked.
     */
    fun recordClicked(page: Page, item: Item) {
        val recordedPage = mPagesClickStatusList.find { it.page == page }
        if (recordedPage != null) {
            // update the existing PageClickStatusModule
            recordedPage.clickedItems.add(item)
        } else {
            // add new PageClickStatusModule
            mPagesClickStatusList.add(
                PageClickStatusModule(
                    page = page,
                    clickedItems = mutableListOf(item)
                )
            )
        }
    }

    /**
     * @return whether the given item on that page is clicked
     */
    fun getIsClicked(page: Page, item: Item): Boolean {
        return mPagesClickStatusList.find { it.page == page }?.clickedItems?.contains(item) == true
    }
}