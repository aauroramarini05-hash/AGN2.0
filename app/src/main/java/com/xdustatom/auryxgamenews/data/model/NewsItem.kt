package com.xdustatom.auryxgamenews.data.model

import java.util.Date

data class NewsItem(
    val title: String,
    val description: String,
    val link: String,
    val pubDate: Date,
    val source: String
)
