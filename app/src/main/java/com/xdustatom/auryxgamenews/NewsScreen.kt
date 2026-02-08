package com.xdustatom.auryxgamenews

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xdustatom.auryxgamenews.data.NewsRepository
import com.xdustatom.auryxgamenews.data.RssItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val items: List<RssItem>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(repository: NewsRepository) {
    val context = LocalContext.current
    var state: NewsUiState by remember { mutableStateOf<NewsUiState>(NewsUiState.Loading) }

    fun openLink(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    suspend fun load() {
        state = NewsUiState.Loading
        state = try {
            val items = withContext(Dispatchers.IO) { repository.fetchAll() }
            if (items.isEmpty()) NewsUiState.Error("Nessuna notizia trovata. Riprova.")
            else NewsUiState.Success(items)
        } catch (t: Throwable) {
            NewsUiState.Error(t.message ?: "Errore sconosciuto")
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Auryx Game News") }) }
    ) { padding ->
        when (val s = state) {
            is NewsUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Caricamento ultime notizie…")
                    Spacer(Modifier.height(12.dp))
                    Text("Fonti: IGN, Eurogamer, GameSpot")
                }
            }

            is NewsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Errore: ${s.message}")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { state = NewsUiState.Loading }) { Text("Riprova") }
                    LaunchedEffect(state) {
                        if (state is NewsUiState.Loading) load()
                    }
                }
            }

            is NewsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(s.items, key = { it.link }) { item ->
                        NewsCard(item = item, onClick = { openLink(item.link) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(item: RssItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = item.title, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${item.source} • ${item.pubDate ?: "data non disponibile"}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val desc = item.description
            if (!desc.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = desc, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
