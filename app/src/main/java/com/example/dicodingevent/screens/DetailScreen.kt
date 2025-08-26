package com.example.dicodingevent.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dicodingevent.data.viewmodel.EventViewModel
import com.example.dicodingevent.data.viewmodel.EventViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    eventId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory.getInstance(context)
    )
    val scope = rememberCoroutineScope()

    val event by viewModel.getEventById(eventId).observeAsState()

    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        event?.let { currentEvent ->
            isFavorite = currentEvent.isFavorite
        }
    }

    event?.let { currentEvent ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Event",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isFavorite = !isFavorite
                                viewModel.toggleFavorite(currentEvent)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentEvent.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorite" else "Add to favorite",
                            tint = if (currentEvent.isFavorite) Color.Red else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event Image Cover
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentEvent.mediaCover)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentEvent.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Event Title
                Text(
                    text = currentEvent.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )

                // Category Badge
                Surface(
                    color = Color(0xFF2196F3),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.width(IntrinsicSize.Min)
                ) {
                    Text(
                        text = currentEvent.category,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Event Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Owner
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Owner",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = currentEvent.ownerName,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = currentEvent.cityName,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            color = Color.Gray.copy(alpha = 0.3f)
                        )

                        // Date and Time
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Waktu Pelaksanaan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "${formatDate(currentEvent.beginTime)} - ${
                                    formatDate(
                                        currentEvent.endTime
                                    )
                                }",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "${formatTime(currentEvent.beginTime)} - ${
                                    formatTime(
                                        currentEvent.endTime
                                    )
                                } WIB",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            color = Color.Gray.copy(alpha = 0.3f)
                        )

                        // Quota
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kuota Pendaftar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "${currentEvent.registrants}/${currentEvent.quota}",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Summary Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ringkasan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = currentEvent.summary,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Description Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Deskripsi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = parseHtmlText(currentEvent.description),
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Register Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, currentEvent.link.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Daftar Sekarang",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun parseHtmlText(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}

private fun formatDate(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val date = inputFormat.parse(dateTime)
        date?.let { outputFormat.format(it) } ?: dateTime
    } catch (e: Exception) {
        dateTime
    }
}

private fun formatTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val date = inputFormat.parse(dateTime)
        date?.let { outputFormat.format(it) } ?: dateTime
    } catch (e: Exception) {
        dateTime
    }
}