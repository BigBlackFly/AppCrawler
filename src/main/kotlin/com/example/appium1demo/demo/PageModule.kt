package com.example.appium1demo.demo

data class PageModule(
    // refined appium pageSource xml string
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