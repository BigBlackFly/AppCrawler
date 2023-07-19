package com.martin.appcrawler.data

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
        if (other.activity != this.activity) {
            return false
        }
        if (other.pageSource != this.pageSource) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = activity.hashCode()
        result = 31 * result + pageSource.hashCode()
        return result
    }

    fun md5(): String {
        return activity + "_" + SecureUtil.md5(this.pageSource)
    }
}