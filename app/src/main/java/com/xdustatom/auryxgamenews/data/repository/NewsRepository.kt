package com.xdustatom.auryxgamenews.data.repository

import com.xdustatom.auryxgamenews.data.model.NewsItem
import com.xdustatom.auryxgamenews.data.parser.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal repository that fetches RSS feeds via HttpURLConnection and parses them.
 */
class NewsRepository {

    private val feeds: List<Pair<String, String>> = listOf(
        "https://feeds.feedburner.com/ign/all" to "IGN",
        "https://www.eurogamer.net/feed" to "Eurogamer",
        "https://www.gamespot.com/feeds/news/" to "GameSpot"
    )

    suspend fun fetchNews(): Result<List<NewsItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val parser = RssParser()
            val allItems = mutableListOf<NewsItem>()

            for ((url, source) in feeds) {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                }

                try {
                    connection.connect()
                    val code = connection.responseCode
                    if (code !in 200..299) {
                        throw IllegalStateException("HTTP $code for $source")
                    }
                    BufferedInputStream(connection.inputStream).use { input ->
                        allItems.addAll(parser.parse(input, source))
                    }
                } finally {
                    connection.disconnect()
                }
            }

            allItems.sortByDescending { it.pubDate }
            allItems
        }
    }
}
