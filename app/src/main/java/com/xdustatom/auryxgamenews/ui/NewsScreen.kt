package com.xdustatom.auryxgamenews.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xdustatom.auryxgamenews.data.model.NewsItem
import com.xdustatom.auryxgamenews.data.repository.NewsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val items: List<NewsItem>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

@Composable
fun NewsScreen(repository: NewsRepository = NewsRepository()) {
    var uiState by remember { mutableStateOf<NewsUiState>(NewsUiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        load(repository) { uiState = it }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Auryx Game News") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is NewsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is NewsUiState.Error -> {
                    // UI is never blank: show a retry surface.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                uiState = NewsUiState.Loading
                                scope.launch { load(repository) { uiState = it } }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Failed to load news. Tap to retry.\n${state.message}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is NewsUiState.Success -> {
                    NewsList(state.items)
                }
            }
        }
    }
}

private suspend fun load(repository: NewsRepository, update: (NewsUiState) -> Unit) {
    val result = repository.fetchNews()
    result.onSuccess { items ->
        if (items.isEmpty()) update(NewsUiState.Error("No news items found"))
        else update(NewsUiState.Success(items))
    }.onFailure { e ->
        update(NewsUiState.Error(e.message ?: "Unknown error"))
    }
}

@Composable
private fun NewsList(items: List<NewsItem>) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.source, style = MaterialTheme.typography.labelSmall)
                        Text(text = dateFormatter.format(item.pubDate), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
