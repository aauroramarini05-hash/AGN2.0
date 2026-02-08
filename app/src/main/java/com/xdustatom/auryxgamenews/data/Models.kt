package com.xdustatom.auryxgamenews.data

data class RssFeedSource(
    val name: String,
    val url: String
)

data class RssItem(
    val title: String,
    val description: String?,
    val link: String,
    val pubDate: String?,
    val source: String
)
