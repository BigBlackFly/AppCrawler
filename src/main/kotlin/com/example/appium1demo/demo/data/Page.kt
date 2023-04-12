package com.example.appium1demo.demo.data

data class Page(
    // refined appium pageSource xml string
    val pageSource: String
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Page) {
            return false
        }
        return other.pageSource == this.pageSource
    }

    override fun hashCode(): Int {
        return pageSource.hashCode()
    }
}