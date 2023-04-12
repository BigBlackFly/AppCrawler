package com.example.appium1demo.demo.data

import cn.hutool.crypto.SecureUtil

data class Page(
    val activity: String,
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

    fun md5(): String {
        return activity + "_" + SecureUtil.md5(this.pageSource)
    }
}