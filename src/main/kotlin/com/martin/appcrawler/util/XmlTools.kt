package com.martin.appcrawler.util

import org.dom4j.Document
import org.dom4j.Element

object XmlTools {
    fun filterDocument(document: Document, attributes: List<String>): String {
        val root = document.rootElement
        processChild(root, attributes)
        return document.asXML()
    }

    private fun processChild(element: Element, attributes: List<String>) {
        val elements = element.elements()
        for (child in elements) {
            val attributeIterator = child.attributeIterator()
            while (attributeIterator.hasNext()) {
                val item = attributeIterator.next()
                if (!contains(attributes, item.name)) {
                    attributeIterator.remove()
                    item.detach()
                }
            }
            processChild(child, attributes)
        }
    }

    private fun contains(attributes: List<String>, targetAttribute: String): Boolean {
        for (item in attributes) {
            if (item == targetAttribute) {
                return true
            }
        }
        return false
    }
}