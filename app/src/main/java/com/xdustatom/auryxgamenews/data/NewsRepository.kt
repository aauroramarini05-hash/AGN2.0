package com.xdustatom.auryxgamenews.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NewsRepository {

    private val sources = listOf(
        RssFeedSource("IGN", "https://www.ign.com/articles?format=rss"),
        RssFeedSource("Eurogamer", "https://www.eurogamer.net/feed"),
        RssFeedSource("GameSpot", "https://www.gamespot.com/feeds/mashup/")
    )

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val parser = RssParser()

    fun getSources(): List<RssFeedSource> = sources

    fun fetchAll(): List<RssItem> {
        val all = mutableListOf<RssItem>()
        for (s in sources) {
            val req = Request.Builder()
                .url(s.url)
                .header("User-Agent", "AuryxGameNews/1.207.01")
                .build()

            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use
                val body = resp.body ?: return@use
                body.byteStream().use { stream ->
                    all += parser.parse(sourceName = s.name, input = stream)
                }
            }
        }
        return all.distinctBy { it.link }
    }
}
