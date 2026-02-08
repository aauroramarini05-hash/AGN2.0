package com.xdustatom.auryxgamenews.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class RssParser {

    fun parse(sourceName: String, input: InputStream): List<RssItem> {
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
        val parser = factory.newPullParser()
        parser.setInput(input, null)

        val items = mutableListOf<RssItem>()

        var event = parser.eventType
        var insideItem = false

        var title: String? = null
        var description: String? = null
        var link: String? = null
        var pubDate: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "item", "entry" -> {
                            insideItem = true
                            title = null
                            description = null
                            link = null
                            pubDate = null
                        }
                        "title" -> if (insideItem) title = readText(parser)
                        "description", "summary" -> if (insideItem) description = readText(parser)
                        "link" -> if (insideItem) {
                            val href = parser.getAttributeValue(null, "href")
                            link = href ?: readText(parser)
                        }
                        "pubdate", "published", "updated" -> if (insideItem) pubDate = readText(parser)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name.lowercase()) {
                        "item", "entry" -> {
                            insideItem = false
                            val t = title?.trim().orEmpty()
                            val l = link?.trim().orEmpty()
                            if (t.isNotEmpty() && l.isNotEmpty()) {
                                items += RssItem(
                                    title = t,
                                    description = description?.let { stripHtml(it) }?.takeIf { it.isNotBlank() },
                                    link = l,
                                    pubDate = pubDate?.trim(),
                                    source = sourceName
                                )
                            }
                        }
                    }
                }
            }
            event = parser.next()
        }

        return items
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text ?: ""
            parser.nextTag()
        }
        return result
    }

    private fun stripHtml(input: String): String {
        return input
            .replace(Regex("<!\[CDATA\[(.*?)\]\]>", setOf(RegexOption.DOT_MATCHES_ALL)), "$1")
            .replace(Regex("<[^>]*>"), " ")
            .replace(Regex("\s+"), " ")
            .trim()
    }
}
