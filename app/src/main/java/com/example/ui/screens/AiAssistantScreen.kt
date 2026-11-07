package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.UserPreferences
import com.example.data.model.WeatherBundle
import com.example.ui.AiBriefingUiState
import com.example.ui.AiChatMessage
import com.example.ui.theme.ExtremeHeatCrimson
import com.example.ui.theme.GoldenHourOrange
import com.example.ui.theme.IonCyan
import com.example.ui.theme.OasisEmerald
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.StormViolet
import com.example.util.AppStrings.tr
import kotlin.math.roundToInt

@Composable
fun AiAssistantScreen(
    bundle: WeatherBundle,
    prefs: UserPreferences,
    briefingState: AiBriefingUiState,
    chatMessages: List<AiChatMessage>,
    isStreaming: Boolean,
    onGenerateBriefing: () -> Unit,
    onAskQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = prefs.language
    var questionInput by remember { mutableStateOf("") }

    // Automatically request daily briefing on first open if idle
    LaunchedEffect(bundle.city.id) {
        if (briefingState is AiBriefingUiState.Idle) {
            onGenerateBriefing()
        }
    }

    val samplePrompts = if (lang == AppLanguage.ARABIC) {
        listOf(
            "هل ستمطر في الناصرية الليلة؟",
            "هل طقس الغد مناسب للأنشطة الخارجية في ${bundle.city.nameAr}؟",
            "ما هي أشد ساعات اليوم حرارة؟",
            "كيف سيتغير الطقس خلال الـ 24 ساعة القادمة؟",
            "قارن بين طقس بغداد والبصرة وأربيل اليوم"
        )
    } else {
        listOf(
            "Will it rain in Nasiriyah tonight?",
            "Is tomorrow suitable for outdoor activities in ${bundle.city.nameEn}?",
            "What is the hottest period of the day?",
            "How will the weather change during the next 24 hours?",
            "Compare weather between Baghdad, Basra, and Erbil right now"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("ai_assistant_screen")
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            // 1. Header & Telemetry Grounding Guarantee
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = IonCyan
                            )
                            Text(
                                text = tr(lang, "Atmosphere AI Intelligence", "الذكاء الاصطناعي الجوي Atmosphere AI"),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = tr(
                                lang,
                                "Grounded strictly in live telemetry for ${bundle.city.nameEn} (${bundle.current.temperatureC.roundToInt()}°C, AQI ${bundle.airQuality.usAqi})",
                                "مبني بدقة على قراءات الرصد الفعلي لمدينة ${bundle.city.nameAr} (${bundle.current.temperatureC.roundToInt()}°م، AQI ${bundle.airQuality.usAqi})"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onGenerateBriefing,
                        modifier = Modifier.testTag("refresh_ai_briefing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = tr(lang, "Regenerate Briefing", "تحديث الملخص الذكي"),
                            tint = IonCyan
                        )
                    }
                }
            }

            // 2. Daily AI Weather Briefing & Smart Recommendations
            item {
                when (briefingState) {
                    is AiBriefingUiState.Idle, is AiBriefingUiState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                                .border(1.dp, IonCyan.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = IonCyan,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "Synthesizing meteorological briefing with Gemini AI...",
                                    "جارٍ تحليل بيانات الرصد الجوي عبر Gemini AI..."
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    is AiBriefingUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(ExtremeHeatCrimson.copy(alpha = 0.14f))
                                .border(1.dp, ExtremeHeatCrimson.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = briefingState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = onGenerateBriefing,
                                colors = ButtonDefaults.buttonColors(containerColor = IonCyan)
                            ) {
                                Text(tr(lang, "Retry AI Briefing", "إعادة المحاولة"))
                            }
                        }
                    }

                    is AiBriefingUiState.Success -> {
                        val intel = briefingState.intelligence
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Morning Briefing Hero
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                                    .border(1.dp, IonCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tr(lang, "Intelligent Daily Weather Briefing", "النشرة الجوية الذكية اليومية"),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = intel.morningBriefing,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AiRecommendationCard(
                                icon = Icons.Default.DirectionsRun,
                                title = tr(lang, "Outdoor Activities", "الأنشطة الخارجية"),
                                body = intel.outdoorActivityAdvice,
                                accent = OasisEmerald
                            )
                            AiRecommendationCard(
                                icon = Icons.Default.Checkroom,
                                title = tr(lang, "Clothing & Gear", "الملابس المقترحة"),
                                body = intel.clothingSuggestion,
                                accent = SolarAmber
                            )
                            AiRecommendationCard(
                                icon = Icons.Default.DirectionsCar,
                                title = tr(lang, "Commute & Travel", "التنقل والسفر"),
                                body = intel.commuteAndTravelAdvice,
                                accent = StormViolet
                            )
                            AiRecommendationCard(
                                icon = Icons.Default.HealthAndSafety,
                                title = tr(lang, "Heat & Dust Precautions", "الوقاية من الحر والغبار"),
                                body = intel.heatAndDustSafetyNote,
                                accent = GoldenHourOrange
                            )
                        }
                    }
                }
            }

            // 3. One-Tap Meteorological Question Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = tr(lang, "Ask Natural-Language Weather Questions", "اسأل باللغة الطبيعية عن حالة الطقس"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(samplePrompts) { prompt ->
                            SuggestionChip(
                                onClick = { onAskQuestion(prompt) },
                                label = { Text(prompt, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
            }

            // 4. Conversational Q&A History
            items(chatMessages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when {
                                    msg.isUser -> IonCyan.copy(alpha = 0.22f)
                                    msg.isError -> ExtremeHeatCrimson.copy(alpha = 0.18f)
                                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    msg.isUser -> IonCyan.copy(alpha = 0.5f)
                                    msg.isError -> ExtremeHeatCrimson
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                RoundedCornerShape(20.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (msg.isUser) {
                                tr(lang, "You", "أنت")
                            } else {
                                "Atmosphere AI"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (msg.isUser) IonCyan else SolarAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (msg.isStreaming && msg.text.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = IonCyan,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = tr(lang, "Analyzing live telemetry...", "جارٍ تحليل البيانات الجوية..."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Bottom Question Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = questionInput,
                onValueChange = { questionInput = it },
                placeholder = {
                    Text(
                        tr(
                            lang,
                            "Ask e.g. 'Will it rain in Nasiriyah tonight?'",
                            "اسأل مثلاً: هل ستمطر في الناصرية الليلة؟"
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_question_input"),
                singleLine = true,
                shape = RoundedCornerShape(20.dp)
            )

            IconButton(
                onClick = {
                    if (questionInput.isNotBlank() && !isStreaming) {
                        onAskQuestion(questionInput)
                        questionInput = ""
                    }
                },
                enabled = questionInput.isNotBlank() && !isStreaming,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(IonCyan)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = tr(lang, "Send question", "إرسال السؤال"),
                    tint = Color(0xFF041326)
                )
            }
        }
    }
}

@Composable
private fun AiRecommendationCard(
    icon: ImageVector,
    title: String,
    body: String,
    accent: Color
) {
    if (body.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
