package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ResetExecutionResult
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkInputBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MediaAccentCyan
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ResetCenterSection(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val lastResetResult by viewModel.lastResetResult.collectAsState()

    var resetTypeToConfirm by remember { mutableStateOf<String?>(null) } // "PROJECTS", "FINANCE", "SETTINGS", "BASE", "ALL"
    var hardResetConfirmedByUser by remember { mutableStateOf(false) }
    var hardResetTypedText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ErrorRed.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مرکز بازنشانی و پاک‌سازی تفکیک‌شده",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 14.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "امکان بازنشانی هدفمند هر بخش از نرم‌افزار بدون آسیب به سایر بخش‌ها",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 2. Safety Caution Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ErrorRed.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "توجه: عملیات پاک‌سازی دیتابیس قطعی و غیرقابل بازگشت هستند. پیش از انجام هرگونه بازنشانی، توصیه اکید می‌شود از بخش «ایمنی و بک‌آپ» یک نسخه پشتیبان صادر فرمایید.",
                    color = ErrorRed,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // 3. Last Reset Feedback Card (if any)
        if (lastResetResult != null) {
            val res = lastResetResult!!
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(res.title, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${res.description} (زمان: ${res.timestamp} • ${res.itemsAffectedCount} قلم)", color = TextSecondary, fontSize = 10.5.sp)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.clearResetResult() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 4. Granular Reset Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Option 1: Reset Projects Only
            GranularResetCard(
                title = "۱. پاک‌سازی پروژه‌ها، کلیپ‌ها و کارکرد",
                subtitle = "حذف کلیه پروژه‌های در حال تدوین و آرشیو به همراه جلسات تایمر",
                icon = Icons.Default.Movie,
                erasedItems = listOf("کلیه پروژه‌های جاری و تحویل‌شده", "کلیه کلیپ‌ها و اصلاحیه‌ها", "تمامی سوابق تایمر و زمان‌سنج"),
                preservedItems = listOf("فهرست استودیوها و کلیپ‌های مرجع", "سوابق فاکتور و تنظیمات برنامه"),
                buttonText = "پاک‌سازی پروژه‌ها و کارکرد",
                onClick = { resetTypeToConfirm = "PROJECTS" }
            )

            // Option 2: Reset Finance Only
            GranularResetCard(
                title = "۲. پاک‌سازی سوابق مالی و دریافتی‌ها",
                subtitle = "حذف تراکنش‌های پرداخت و بازگرداندن پروژه‌ها به وضعیت پرداخت‌نشده",
                icon = Icons.Default.Receipt,
                erasedItems = listOf("کلیه فیش‌ها و سوابق واریزی استودیوها", "وضعیت تسویه‌شده پروژه‌ها به بدهکار بازمی‌گردد"),
                preservedItems = listOf("مشخصات پروژه‌ها و تایمرها دست‌نخورده می‌مانند", "استودیوها و تنظیمات حفظ می‌شوند"),
                buttonText = "پاک‌سازی سوابق مالی",
                onClick = { resetTypeToConfirm = "FINANCE" }
            )

            // Option 3: Reset Settings Only
            GranularResetCard(
                title = "۳. بازنشانی تنظیمات به حالت اولیه کارخانه",
                subtitle = "بازگرداندن مشخصات فاکتور، اهداف روزانه و پکیج‌ها به پیش‌فرض",
                icon = Icons.Default.SettingsBackupRestore,
                erasedItems = listOf("عنوان برند، شماره کارت و توضیحات فاکتور", "ساعت هدف روزانه، قیمت‌های سریع و پکیج‌ها"),
                preservedItems = listOf("کلیه پروژه‌ها و داده‌های مالی کاملاً دست‌نخورده می‌مانند", "استودیوها حفظ می‌شوند"),
                buttonText = "بازنشانی تنظیمات برنامه",
                onClick = { resetTypeToConfirm = "SETTINGS" }
            )

            // Option 4: Reset Base Data Only
            GranularResetCard(
                title = "۴. بازنشانی آتلیه‌ها و کلیپ‌های مرجع",
                subtitle = "ریست کردن اسامی استودیوها و دسته‌بندی کلیپ‌ها به داده‌های اولیه",
                icon = Icons.Default.Store,
                erasedItems = listOf("استودیوهای سفارشی به ۲ استودیوی اولیه ریست می‌شوند", "کلیپ‌های مرجع به ۱۰ عنوان استاندارد ریست می‌شوند"),
                preservedItems = listOf("کلیه پروژه‌ها، امور مالی و تنظیمات دست‌نخورده باقی می‌مانند"),
                buttonText = "بازنشانی استودیوها و کلیپ‌ها",
                onClick = { resetTypeToConfirm = "BASE" }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Option 5: Hard Reset All (Full Factory Wipe)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ErrorRed.copy(alpha = 0.10f),
                border = BorderStroke(1.5.dp, ErrorRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(ErrorRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("۵. بازنشانی کلی و خام‌سازی کامل نرم‌افزار (Hard Reset)", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("پاک‌سازی کامل پایگاه داده SQLite و بارگذاری دیتای اولیه کارخانه", color = TextSecondary, fontSize = 10.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "با اجرای این عملیات، تمامی پروژه‌ها، سوابق مالی، تایمرها، آتلیه‌ها، کلیپ‌ها و تنظیمات شخصی‌سازی‌شده به طور کامل و غیرقابل بازگشت پاک خواهند شد و برنامه مانند روز اول نصب اجرا می‌شود.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            hardResetConfirmedByUser = false
                            hardResetTypedText = ""
                            resetTypeToConfirm = "ALL"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_hard_reset_all"),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خام‌سازی و ریست کلی نرم‌افزار", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // =============================================================================================
    // RESET CONFIRMATION DIALOGS
    // =============================================================================================
    if (resetTypeToConfirm != null) {
        val type = resetTypeToConfirm!!

        if (type == "ALL") {
            // High-Safety Hard Reset Dialog with Checkbox Verification
            AlertDialog(
                onDismissRequest = { resetTypeToConfirm = null },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.85f),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تأیید خام‌سازی کامل نرم‌افزار", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "هشدار جدی: شما در حال حذف کامل تمام اطلاعات ثبت‌شده در کاترلاگ هستید. این عملیات شامل تمام پروژه‌ها، امور مالی و تنظیمات می‌شود.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkInputBg)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = hardResetConfirmedByUser,
                                onCheckedChange = { hardResetConfirmedByUser = it },
                                colors = CheckboxDefaults.colors(checkedColor = ErrorRed, checkmarkColor = Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "متوجه عواقب حذف تمام اطلاعات هستم و تأیید می‌کنم.",
                                color = ErrorRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetData { result ->
                                Toast.makeText(context, result.description, Toast.LENGTH_LONG).show()
                            }
                            resetTypeToConfirm = null
                        },
                        enabled = hardResetConfirmedByUser,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("اجرای قطعی خام‌سازی کامل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetTypeToConfirm = null }) {
                        Text("انصراف", color = TextMuted)
                    }
                },
                containerColor = DarkCard,
                shape = RoundedCornerShape(16.dp)
            )
        } else {
            // Granular Confirmation Dialog
            val (titleText, msgText, executeAction) = when (type) {
                "PROJECTS" -> Triple(
                    "تأیید پاک‌سازی پروژه‌ها و کارکرد",
                    "آیا مطمئن هستید که می‌خواهید تمامی پروژه‌ها، کلیپ‌ها، اصلاحات و سوابق زمانی تایمر را حذف کنید؟\n\nاستودیوها، مالی و تنظیمات حفظ خواهند شد.",
                    { viewModel.resetProjectsOnly { res -> Toast.makeText(context, res.description, Toast.LENGTH_SHORT).show() } }
                )
                "FINANCE" -> Triple(
                    "تأیید پاک‌سازی سوابق مالی",
                    "آیا مطمئن هستید که می‌خواهید تمامی تراکنش‌های ثبت‌شده و پرداختی‌ها را پاک کنید؟\n\nمشخصات پروژه‌ها و تنظیمات حفظ خواهند شد.",
                    { viewModel.resetFinanceOnly { res -> Toast.makeText(context, res.description, Toast.LENGTH_SHORT).show() } }
                )
                "SETTINGS" -> Triple(
                    "تأیید بازنشانی تنظیمات",
                    "آیا مطمئن هستید که می‌خواهید مشخصات برند، فاکتور، هدف روزانه و قیمت‌ها را به مقادیر اولیه بازگردانید؟\n\nداده‌های پروژه‌ها و مالی حفظ خواهند شد.",
                    { viewModel.resetSettingsOnly { res -> Toast.makeText(context, res.description, Toast.LENGTH_SHORT).show() } }
                )
                else -> Triple(
                    "تأیید بازنشانی آتلیه‌ها و کلیپ‌ها",
                    "آیا مطمئن هستید که می‌خواهید فهرست آتلیه‌ها و کلیپ‌های مرجع به فهرست استاندارد اولیه بازگردد؟\n\nپروژه‌های ثبت‌شده حفظ خواهند شد.",
                    { viewModel.resetBaseDataOnly { res -> Toast.makeText(context, res.description, Toast.LENGTH_SHORT).show() } }
                )
            }

            AlertDialog(
                onDismissRequest = { resetTypeToConfirm = null },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.85f),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(titleText, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }
                },
                text = {
                    Text(msgText, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            executeAction()
                            resetTypeToConfirm = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تأیید و اجرای پاک‌سازی", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetTypeToConfirm = null }) {
                        Text("انصراف", color = TextMuted)
                    }
                },
                containerColor = DarkCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// =============================================================================================
// SUB-COMPONENTS
// =============================================================================================

@Composable
private fun GranularResetCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    erasedItems: List<String>,
    preservedItems: List<String>,
    buttonText: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkCard,
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryPurple.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitle, color = TextMuted, fontSize = 10.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Erased bullet points
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("موارد مشمول پاک‌سازی:", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                erasedItems.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(item, color = TextSecondary, fontSize = 10.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text("موارد مصون و حفظ‌شده:", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                preservedItems.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(item, color = TextMuted, fontSize = 10.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(buttonText, color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}
