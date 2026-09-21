package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

/**
 * Minimalist, Premium & Professional Top Header for CutterLog.
 *
 * Designed according to strict specifications:
 * 1. Brand Identity (CutterLog logo & title - bold & clear)
 * 2. Daily inspiring editing/story quote (deterministic based on day of year, subtle & non-competing)
 * 3. Quick Settings access (clean, accessible, touch-friendly icon)
 */
@Composable
fun CutterLogTopHeader(
    onSettingsClick: () -> Unit,
    isSettingsSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Curated collection of short, professional, and inspiring quotes for video editors
    val quotes = remember {
        listOf(
            "هر فریم، بخشی از یک داستان است.",
            "امروز هم یک داستان تازه بساز.",
            "جزئیات، تفاوت را می‌سازند.",
            "فریم به فریم، داستان شکل می‌گیرد.",
            "یک تدوین خوب، احساس می‌شود.",
            "هر پروژه، یک خاطره ماندگار است.",
            "کمتر شلوغ کن، بیشتر خلق کن.",
            "داستان از جایی شروع می‌شود که تدوین آغاز می‌شود.",
            "هر کات، بخشی از روایت است.",
            "خاطره‌ها را بهتر روایت کن."
        )
    }

    // Deterministic quote of the day (stable throughout the day, rotates every calendar day)
    val dailyQuote = remember(quotes) {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        quotes[dayOfYear % quotes.size]
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = DarkCard,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Right side in RTL (Start): Logo + Title + Daily Quote
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // CutterLog Logo Asset
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(DarkSurface)
                            .border(1.dp, BorderDark, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cutterlog_logo_card),
                            contentDescription = "لوگوی کاترلاگ",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Daily Quote
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "کاترلاگ",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            letterSpacing = 0.2.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = dailyQuote,
                            color = TextSecondary.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Left side in RTL (End): Settings Icon Button (48dp touch-friendly M3 IconButton)
                var rotationClicks by remember { mutableStateOf(0) }
                val rotation by animateFloatAsState(
                    targetValue = rotationClicks * 180f,
                    animationSpec = tween(durationMillis = 400),
                    label = "gearRotation"
                )

                IconButton(
                    onClick = {
                        rotationClicks++
                        onSettingsClick()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("header_settings_button"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isSettingsSelected) PrimaryPurple else TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "تنظیمات کاترلاگ",
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer(rotationZ = rotation)
                    )
                }
            }

            // Subtle hair-line separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderDark.copy(alpha = 0.4f))
            )
        }
    }
}
