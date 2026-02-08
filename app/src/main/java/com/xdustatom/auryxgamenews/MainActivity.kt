package com.xdustatom.auryxgamenews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.xdustatom.auryxgamenews.data.NewsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = NewsRepository()
        setContent {
            MaterialTheme {
                NewsScreen(repository = repository)
            }
        }
    }
}
