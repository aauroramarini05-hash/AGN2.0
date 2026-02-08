package com.xdustatom.auryxgamenews.data.parser

import androidx.core.text.HtmlCompat
import com.xdustatom.auryxgamenews.data.model.NewsItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Native RSS XML parser using XmlPullParser (no external RSS libraries).
 */
class RssParser {

    fun parse(stream: InputStream, sourceName: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()

        val factory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = false
        }
        val parser = factory.newPullParser().apply {
            setInput(stream, null)
        }

        var event = parser.eventType
        var insideItem = false

        var currentText: String? = null
        var title: String? = null
        var description: String? = null
        var link: String? = null
        var pubDateString: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("item", ignoreCase = true)) {
                        insideItem = true
                        title = null
                        description = null
                        link = null
                        pubDateString = null
                    }
                }

                XmlPullParser.TEXT -> {
                    currentText = parser.text
                }

                XmlPullParser.END_TAG -> {
                    if (insideItem) {
                        when (parser.name.lowercase(Locale.US)) {
                            "title" -> title = currentText?.trim()
                            "description" -> description = currentText?.trim()
                            "link" -> link = currentText?.trim()
                            "pubdate" -> pubDateString = currentText?.trim()
                            "dc:date" -> if (pubDateString == null) pubDateString = currentText?.trim()
                            "item" -> {
                                items.add(
                                    NewsItem(
                                        title = title ?: "(untitled)",
                                        description = description?.let(::htmlToPlainText) ?: "",
                                        link = link ?: "",
                                        pubDate = parseDate(pubDateString),
                                        source = sourceName
                                    )
                                )
                                insideItem = false
                            }
                        }
                    }
                    currentText = null
                }
            }
            event = parser.next()
        }

        return items
    }

    private fun htmlToPlainText(html: String): String {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .trim()
    }

    private fun parseDate(raw: String?): Date {
        if (raw.isNullOrBlank()) return Date()

        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm Z",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )

        for (pattern in formats) {
            try {
                return SimpleDateFormat(pattern, Locale.US).parse(raw) ?: continue
            } catch (_: ParseException) {
                // try next
            }
        }

        return Date()
    }
}
