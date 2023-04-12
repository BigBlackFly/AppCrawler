package com.example.appium1demo.demo.data

/**
 * a step, that triggers a page navigation.
 *
 * @param page on which page
 * @param item clicked which item
 *
 */
data class Step(
    val page: Page,
    val item: Item
    // TODO: add property: action. it's value can be click, swipe, longClick, etc.
)