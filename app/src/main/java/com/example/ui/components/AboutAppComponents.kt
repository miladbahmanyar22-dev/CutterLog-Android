package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.R
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkInputBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MediaAccentCyan
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianUtils

/**
 * Clean, modern About Application button for Settings.
 */
@Composable
fun AboutAppTopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("about_app_top_button"),
        shape = RoundedCornerShape(14.dp),
        color = DarkCard,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryPurple.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "درباره کاترلاگ و مشخصات نسخه",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "نسخه ${PersianUtils.faNum(BuildConfig.VERSION_NAME)} • هویت نرم‌افزار، امکانات و مشخصات فنی",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Text(
                    text = "مشاهده جزئیات",
                    color = MediaAccentCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Official, comprehensive, and beautiful About Dialog showcasing version 1.0.5,
 * mission statement, engineering architecture, and core modules.
 */
@Composable
fun AboutAppDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("about_app_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.2.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryPurple.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "شناسنامه رسمی نرم‌افزار",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // App Hero Identity
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_cutterlog_logo_card),
                        contentDescription = "نشان کاترلاگ",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "نرم‌افزار جامع کاترلاگ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "سیستم یکپارچه مدیریت کارکرد، استودیوها و مالی تدوین‌گران",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Version & Release Pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryPurple.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "نسخه ${PersianUtils.faNum(BuildConfig.VERSION_NAME)}",
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SuccessGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نسخه پایدار و نهایی",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mission & Philosophy Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ماموریت و اهداف نرم‌افزار",
                            color = MediaAccentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "کاترلاگ (CutterLog) اختصاصاً برای تدوین‌گران و فعالان صنعت رسانه طراحی گردیده تا چالش‌های مربوط به ثبت دقیق زمان پروژه‌ها، محاسبه دستمزدهای پکیجی و دقیقه‌ای، هماهنگی با استودیوهای کاری و صدور فاکتورهای رسمی و تسویه‌حساب‌های مالی را به شکلی مدرن، آفلاین و متمرکز مرتفع سازد.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Capabilities Grid
                Text(
                    text = "ویژگی‌ها و قابلیت‌های کلیدی نسخه ۱.۰.۵",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureDetailItem(
                        icon = Icons.Default.Speed,
                        title = "پایش زمان و تایمر متمرکز پروژه‌ها",
                        desc = "تایمر زنده با پشتیبانی از توقف موقت، سقف کاری روزانه و هشدار هوشمند صوتی"
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.VideoLibrary,
                        title = "مدیریت چند استودیویی و پکیج‌ها",
                        desc = "تفکیک اختصاصی کلیپ‌ها، قیمت‌گذاری پیشرفته دقیقه‌ای و پروژه‌ای"
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.VerifiedUser,
                        title = "صدور پیش‌فاکتور و تسویه‌حساب رسمی",
                        desc = "خروجی تصویری و PDF رسمی به همراه ثبت درصد مالیات، تخفیف و پیش‌پرداخت"
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.Security,
                        title = "حفظ حریم خصوصی و امنیت کامل داده‌ها",
                        desc = "۱۰۰٪ عملکرد آفلاین (Local-First)، پایگاه داده رمزنگاری‌شده Room و بک‌آپ‌گیری JSON"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Engineering Specs
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkInputBg,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "موتور رابط کاربری", color = TextMuted, fontSize = 11.5.sp)
                            Text(text = "Jetpack Compose (Material 3)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "معماری توسعه", color = TextMuted, fontSize = 11.5.sp)
                            Text(text = "Kotlin Clean Architecture + Flow", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close Action
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "بستن شناسنامه نرم‌افزار",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureDetailItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MediaAccentCyan.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MediaAccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
