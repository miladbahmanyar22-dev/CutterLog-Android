package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkInputBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MediaAccentCyan
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

object DeveloperSupportConstants {
    const val DEVELOPER_NAME = "میلاد بهمنیار"
    const val BANK_NAME = "بلوبانک"
    const val CARD_NUMBER_RAW = "6219861980223514"
    const val CARD_NUMBER_FORMATTED = "6219  8619  8022  3514"
    const val CONTACT_PHONE = "09368330224"
    const val SMS_DEFAULT_MESSAGE = "سلام میلاد، من از کاترلاگ حمایت کردم.\nخواستم بابت زمانی که برای توسعه و بهبود برنامه می‌گذاری تشکر کنم. 🙏"
}

/**
 * Eye-catching banner button shown at the top of Settings.
 * Vibrant colorful gradient with enticing message that opens the full developer support sheet/dialog.
 */
@Composable
fun DeveloperSupportTopBannerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_support_top_banner_button"),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFF43F5E),
                    Color(0xFFA855F7),
                    Color(0xFF06B6D4)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFF43F5E).copy(alpha = 0.22f),
                            Color(0xFFA855F7).copy(alpha = 0.25f),
                            Color(0xFF06B6D4).copy(alpha = 0.18f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing/Glowing heart container
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF43F5E),
                        modifier = Modifier.size(38.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "حمایت",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "حمایت از توسعه‌دهنده کاترلاگ",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "با یک فنجان قهوه، به پایداری و رشد برنامه کمک کنید ☕❤️",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Action Capsule Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "مشاهده اطلاعات",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dedicated, beautifully redesigned Developer Support Dialog / View.
 */
@Composable
fun DeveloperSupportDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showSmsFallbackDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(textToCopy: String, toastMessage: String) {
        clipboardManager.setText(AnnotatedString(textToCopy))
        try {
            val systemClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("CutterLog Developer Support", textToCopy)
            systemClipboard?.setPrimaryClip(clip)
        } catch (_: Exception) {
        }
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    fun openSmsComposer() {
        val smsUri = Uri.parse("smsto:${DeveloperSupportConstants.CONTACT_PHONE}")
        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", DeveloperSupportConstants.SMS_DEFAULT_MESSAGE)
            putExtra(Intent.EXTRA_TEXT, DeveloperSupportConstants.SMS_DEFAULT_MESSAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(smsIntent)
        } catch (_: Exception) {
            showSmsFallbackDialog = true
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("developer_support_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.2.dp, Color(0xFFF43F5E).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header with close button and heart banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF43F5E).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "حمایت از توسعه‌دهنده",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "همراهی شما، انگیزه ادامه راه",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Friendly note card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "کاترلاگ با عشق و ساعت‌ها تلاش مستمر برای ارتقای راندمان کاری تدوین‌گران عزیز توسعه یافته است. اگر این ابزار به کار و درآمد شما کمک کرده، مایه افتخار است که با حمایت خود به بهبود و بقای آن یاری رسانید.",
                            color = TextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bank card luxury container
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF0F172A),
                                        Color(0xFF111827)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top bank badge & chip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = MediaAccentCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "کارت بانکی شتاب",
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MediaAccentCyan.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, MediaAccentCyan.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = DeveloperSupportConstants.BANK_NAME,
                                        color = MediaAccentCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Card Number
                            Text(
                                text = "شماره کارت",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF030712).copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, Color(0xFF374151))
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        text = DeveloperSupportConstants.CARD_NUMBER_FORMATTED,
                                        color = Color(0xFFF9FAFB),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.5.sp,
                                        letterSpacing = 2.sp,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            textDirection = TextDirection.Ltr
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                            .testTag("card_number_text")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Card Owner Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "به نام",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DeveloperSupportConstants.DEVELOPER_NAME,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "مالک حساب",
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button: Copy Card Number
                    Button(
                        onClick = {
                            copyToClipboard(
                                textToCopy = DeveloperSupportConstants.CARD_NUMBER_RAW,
                                toastMessage = "شماره کارت کپی شد"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("copy_card_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "کپی شماره کارت ۱۶ رقمی",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    // Button: Send Support SMS
                    OutlinedButton(
                        onClick = { openSmsComposer() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_sms_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.2.dp, BorderDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ارسال پیام محبت‌آمیز حمایت (پیامک)",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Close Button
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "بستن",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Fallback Dialog if SMS is not installed
    if (showSmsFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showSmsFallbackDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .testTag("sms_fallback_dialog"),
            shape = RoundedCornerShape(16.dp),
            containerColor = DarkCard,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "برنامه پیامک در این دستگاه در دسترس نیست.",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "امکان باز کردن خودکار برنامه پیامک در این دستگاه فراهم نشد. می‌توانید شماره تماس و متن پیام را کپی کرده و به صورت دستی ارسال کنید.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            copyToClipboard(
                                textToCopy = DeveloperSupportConstants.CONTACT_PHONE,
                                toastMessage = "شماره تماس کپی شد"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("fallback_copy_phone_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "کپی شماره تماس (${DeveloperSupportConstants.CONTACT_PHONE})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            copyToClipboard(
                                textToCopy = DeveloperSupportConstants.SMS_DEFAULT_MESSAGE,
                                toastMessage = "متن پیام کپی شد"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("fallback_copy_message_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = MediaAccentCyan,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "کپی متن پیام حمایت",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSmsFallbackDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "بستن",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = null
        )
    }
}

/**
 * Modern compact card in Settings bottom that opens the support dialog
 */
@Composable
fun DeveloperSupportCard(
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    DeveloperSupportTopBannerButton(
        onClick = onOpenDetails,
        modifier = modifier
    )
}

/**
 * Overload without onOpenDetails (retains internal state)
 */
@Composable
fun DeveloperSupportCard(
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    DeveloperSupportTopBannerButton(
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        DeveloperSupportDialog(
            onDismissRequest = { showDialog = false }
        )
    }
}
