package com.martin.appcrawler.data

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent

/**
 * a step, that triggers a page navigation.
 *
 * @param page on which page
 * @param item on which item (is null when [action] is [StepAction.BACK])
 * @param action performed which action
 *
 */
data class Step(
        val page: Page,
        val item: Item?,
        val action: StepAction
) {

    /**
     * re-perform this step. is used while back tracing.
     */
    fun action(driver: AndroidDriver) {
        when (this.action) {
            StepAction.BACK -> {
                if (this.item != null) {
                    println("warning: item should be set with null when action = StepAction.BACK")
                }
                driver.pressKey(KeyEvent(AndroidKey.BACK))
            }

            StepAction.CLICK -> {
                if (this.item == null) {
                    println("error: item should not be set with null when action = StepAction.CLICK")
                    return
                }
                this.item.getWebElement(driver).click()
            }

            else -> {
                // TODO: to be implemented.
            }
        }
    }
}

/**
 * I think we need to play back the user's action sometimes. so, the types of action must be limited.
 */
sealed interface StepAction {
    object BACK : StepAction
    object CLICK : StepAction
    object LONG_CLICK : StepAction
    object DOUBLE_CLICK : StepAction
    object SWIPE_UP_TINY : StepAction
    object SWIPE_DOWN_TINY : StepAction
    object SWIPE_LEFT_TINY : StepAction
    object SWIPE_RIGHT_TINY : StepAction
    object SWIPE_UP_HEAVY : StepAction
    object SWIPE_DOWN_HEAVY : StepAction
    object SWIPE_LEFT_HEAVY : StepAction
    object SWIPE_RIGHT_HEAVY : StepAction
}