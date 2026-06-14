package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flashcard
import com.example.data.LESSONS_CONTENT
import com.example.data.LessonContent
import com.example.viewmodel.StudyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FalconStudyApp(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeState by viewModel.theme.collectAsState()
    val subjectState by viewModel.subject.collectAsState()
    val sfxEnabled by viewModel.sfxEnabled.collectAsState()

    // Base colors depending on the active subject (English elegant-violet vs H&G elegant-pink)
    val primaryAccentColor = if (subjectState == "english") Color(0xFFD0BCFF) else Color(0xFFEFB8C8)
    val secondaryAccentColor = if (subjectState == "english") Color(0xFF381E72) else Color(0xFF492532)
    val accentGlowColor = primaryAccentColor.copy(alpha = 0.15f)

    // Dark/Light Colors Override (Elegant Dark custom mapping)
    val isDark = themeState == "dark"
    val bgDeepColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFF4EFF4)
    val bgSurfaceColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFFFF)
    val bgCardColor = if (isDark) Color(0xFF2B2930) else Color(0xFFECE6F0)
    val bgHighlightCardColor = if (isDark) Color(0xFF332D41) else Color(0xFFE8DDFF)
    val textMainColor = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val textMutedColor = if (isDark) Color(0xFF938F99) else Color(0xFF49454F)
    val borderColor = if (isDark) Color(0xFF49454F) else Color(0xFFCAC4D0)

    // Local state for Retro boot check sequence
    var showBootScreen by remember { mutableStateOf(true) }
    var bootStep by remember { mutableStateOf(1) } // 1: error, 2: scan/diagnose, 3: success
    val bootLogs = remember { mutableStateListOf<Pair<String, String>>() } // Text, Type (ok, warn, err, fix)
    var bootProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Fatal Error Screen for 1.8s
        delay(1800)
        bootStep = 2
        
        // Step 2: Diagnostic text stream
        val logs = listOf(
            "Initializing FALCON_KERNEL v2.6.1..." to "warn",
            "Loading English Grammar Module..." to "ok",
            "Loading Vocabulary Units [10 units]..." to "ok",
            "Checking Irregular Verbs Database [105 entries]..." to "ok",
            "ERROR: falcon_kernel.sys — memory corruption detected!" to "err",
            "Attempting auto-repair of IRQL_NOT_LESS_OR_EQUAL..." to "fix",
            "Loading History & Geography Module (الاجتماعيات)..." to "ok",
            "Restoring BAC 2026 Exam Strategy Cache..." to "ok",
            "Rebuilding 10-Day Study Planner..." to "ok",
            "Loading Writing Templates [10 types]..." to "ok",
            "Verifying LocalStorage state..." to "ok",
            "WARNING: Spaced Repetition queue partially corrupted — rebuilding..." to "warn",
            "REPAIR COMPLETE — All flashcard units restored successfully." to "ok",
            "Launching FALCON STUDY ENVIRONMENT..." to "fix"
        )

        logs.forEachIndexed { idx, pair ->
            bootLogs.add(pair)
            bootProgress = (idx + 1).toFloat() / logs.size
            delay(120)
        }
        delay(400)
        
        // Step 3: Success Screen
        bootStep = 3
        viewModel.playVictorySound()
        delay(1200)
        
        // Finish boot
        showBootScreen = false
    }

    if (showBootScreen) {
        // Gorgeous black background Retro terminal diagnostic
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .drawBehind {
                    // Draw retro scanlines
                    val lineSpacing = 12.dp.toPx()
                    for (y in 0..size.height.toInt() step lineSpacing.toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (bootStep) {
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "0xDEAD",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "FALCON STUDY OS — Critical System Failure",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        Text(
                            text = "STOP: 0x000000E1 (0xFFFFC000153A8020, 0x0000000000000002)\n" +
                                   "Module: falcon_kernel.sys — Address: 0xFFFFF8000412C5B0\n" +
                                   "IRQL_NOT_LESS_OR_EQUAL — A problem has been detected and study session was shut down to prevent damage to your exam performance.\n\n" +
                                   "Collecting error info... 85% complete",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, primaryAccentColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "FALCON RECOVERY SYSTEM v2.6.1",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = primaryAccentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                reverseLayout = true,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(bootLogs.reversed()) { log ->
                                    val logColor = when (log.second) {
                                        "ok" -> Color(0xFF10B981)
                                        "warn" -> Color(0xFFF59E0B)
                                        "err" -> Color(0xFFEF4444)
                                        "fix" -> Color(0xFF60A5FA)
                                        else -> Color.White
                                    }
                                    val prefix = when (log.second) {
                                        "ok" -> "[ OK ]"
                                        "warn" -> "[WARN]"
                                        "err" -> "[FAIL]"
                                        "fix" -> "[FIX ]"
                                        else -> " >  "
                                    }
                                    Text(
                                        text = "> $prefix  ${log.first}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = logColor,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { bootProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryAccentColor,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "المرجو الانتظار، جاري تحضير الدروس والممّونات...",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "${(bootProgress * 100).toInt()}%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = primaryAccentColor
                            )
                        }
                    }
                }
                3 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // High-quality custom vector-like emblem representing Falcon
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .drawBehind {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(size.width / 2, 5.dp.toPx())
                                        lineTo(size.width * 0.75f, 35.dp.toPx())
                                        lineTo(size.width / 2, 25.dp.toPx())
                                        lineTo(size.width * 0.25f, 35.dp.toPx())
                                        close()
                                        
                                        moveTo(size.width / 2, 25.dp.toPx())
                                        lineTo(size.width * 0.85f, 55.dp.toPx())
                                        lineTo(size.width / 2, 45.dp.toPx())
                                        lineTo(size.width * 0.15f, 55.dp.toPx())
                                        close()
                                        
                                        moveTo(size.width / 2, 45.dp.toPx())
                                        lineTo(size.width * 0.95f, 80.dp.toPx())
                                        lineTo(size.width / 2, 65.dp.toPx())
                                        lineTo(size.width * 0.05f, 80.dp.toPx())
                                        close()
                                        
                                        moveTo(size.width / 2, 65.dp.toPx())
                                        lineTo(size.width / 2, 95.dp.toPx())
                                    }
                                    drawPath(
                                        path = path,
                                        color = primaryAccentColor,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 3.dp.toPx(),
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )
                                }
                                .shadow(20.dp, CircleShape, spotColor = primaryAccentColor)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "FALCON STUDY",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = primaryAccentColor,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "تم استعادة النظام — جاري دخول الفضاء الدراسي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    } else {
        // App workspace
        MaterialTheme(
            colorScheme = if (isDark) {
                darkColorScheme(primary = primaryAccentColor, secondary = secondaryAccentColor)
            } else {
                lightColorScheme(primary = primaryAccentColor, secondary = secondaryAccentColor)
            }
        ) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgSurfaceColor)
                                .drawBehind {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            // Subject Switcher
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .padding(3.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.setSubject("english") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (subjectState == "english") primaryAccentColor else Color.Transparent,
                                        contentColor = if (subjectState == "english") Color.Black else textMutedColor
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("🇬🇧", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الإنجليزية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.setSubject("hg") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (subjectState == "hg") primaryAccentColor else Color.Transparent,
                                        contentColor = if (subjectState == "hg") Color.White else textMutedColor
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("🇲🇦", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الاجتماعيات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Theme and SFX controllers
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { viewModel.toggleSFX() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(borderColor, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = if (sfxEnabled) "🔊" else "🔈",
                                        fontSize = 15.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleTheme() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(borderColor, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = if (isDark) "☀️" else "🌙",
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    // Modern styled adaptive Bottom Navigation
                    NavigationBar(
                        containerColor = bgCardColor,
                        tonalElevation = 8.dp,
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    ) {
                        val navItems = listOf(
                            Triple("dashboard", "الرئيسية", Icons.Default.Home),
                            Triple("lessons", "الدروس", Icons.Default.Star),
                            Triple("flashcards", "حفظ الكلمات", Icons.Default.Add),
                            Triple("verbs", "الأفعال الشاذة", Icons.Default.Create),
                            Triple("construct", if (subjectState == "english") "الإنشاء" else "مقال منشئ", Icons.Default.Edit)
                        )
                        
                        navItems.forEach { (screenId, label, icon) ->
                            val isSelected = currentScreen == screenId
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setScreen(screenId) },
                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = primaryAccentColor,
                                    selectedTextColor = primaryAccentColor,
                                    unselectedIconColor = textMutedColor,
                                    unselectedTextColor = textMutedColor,
                                    indicatorColor = bgHighlightCardColor
                                )
                            )
                        }
                    }
                },
                containerColor = bgDeepColor
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor, bgHighlightCardColor)
                        "lessons" -> LessonsScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                        "flashcards" -> FlashcardsScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                        "verbs" -> VerbsScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                        "construct" -> {
                            if (subjectState == "english") {
                                WritingAssistantScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                            } else {
                                CustomConstructorScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                            }
                        }
                        "quiz-hub" -> QuizHubScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                        "reviews" -> ReviewSessionScreen(viewModel, primaryAccentColor, textMainColor, textMutedColor, bgCardColor, borderColor)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color,
    bgHighlight: Color
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val dueCount by viewModel.dueCount.collectAsState()
    val masteredCount by viewModel.masteredCount.collectAsState()
    val plannerCompleted by viewModel.completedPlannerDays.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = bgHighlight),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Stylized background element matching the design HTML's falcon/bolt element
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = primaryColor.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp)
                            .graphicsLayer(rotationZ = 12f)
                    )
                    
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "طريقك نحو التميز فـ BAC 🇲🇦 • Daily Focus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ادرس بذكاء مع Falcon study!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تطبيق تفاعلي يجمع بين دروس الإنجليزية الشاملة المرفقة بالشرح والترجمة بالدارجة المغربية، وبين منهجيات التاريخ والجغرافيا الدقيقة لكتابة مقالات متميزة وتحليل الوثائق باحترافية.",
                            fontSize = 13.sp,
                            color = textMuted,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Active Spaced Repetition Due Card
        item {
            val subjectState by viewModel.subject.collectAsState()
            val onContainerColor = if (subjectState == "english") Color(0xFF381E72) else Color(0xFF492532)
            
            Card(
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.startReviewSession() }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جلسة المراجعة المتباعدة • Spaced Repetition",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = onContainerColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (dueCount > 0) "لديك $dueCount بطاقات جاهزة للمراجعة اليوم!" else "أحسنت! أكملت مراجعة جميع بطاقات اليوم.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = onContainerColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اضغط للبدء والتقييم باستخدام خوارزمية SM-2",
                            fontSize = 11.sp,
                            color = onContainerColor.copy(alpha = 0.9f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(onContainerColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Review",
                            tint = onContainerColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Stats Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إحصائيات إنجازاتك اليومية:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    // Card 1
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgCard),
                        border = BorderStroke(1.dp, border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("البطاقات الكلية", fontSize = 11.sp, color = textMuted)
                            Text("$totalCount بطاقة", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textMain)
                        }
                    }
                    // Card 2
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgCard),
                        border = BorderStroke(1.dp, border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("البطاقات المحفوظة", fontSize = 11.sp, color = textMuted)
                            Text("$masteredCount بطاقة", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = textMain)
                        }
                    }
                }
            }
        }

        // Checklist of day planner
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("خطة المراجعة المكثفة (10 أيام):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
                    Text("${plannerCompleted.size}/10 أيام", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = bgCard),
                    border = BorderStroke(1.dp, border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (day in 1..10) {
                            val isCompleted = plannerCompleted.contains(day)
                            val dayText = when(day) {
                                1 -> "اليوم الأول: الأزمنة الكاملة (Tenses)"
                                2 -> "اليوم الثاني: المبني للمجهول ونقل الكلام (Passive & Reported)"
                                3 -> "اليوم الثالث: الجمل الشرطية والتمني (Conditionals & Wish)"
                                4 -> "اليوم الرابع: المودالز وضمائر الوصل (Modals & Relative)"
                                5 -> "اليوم الخامس: التعبير عن الغرض وصيغة ing (Purpose & Gerund)"
                                6 -> "اليوم السادس: روابط الكلام والأفعال المركبة (Linkers & Phrasal)"
                                7 -> "اليوم السابع: الوظائف اللغوية والمقدمة (Functions & Introduction)"
                                8 -> "اليوم الثامن: كتابة المراسلات والإيميلات (Emails & Letters)"
                                9 -> "اليوم التاسع: المقالات والنقد والمراجعة (Essays & Reviews)"
                                10 -> "اليوم العاشر: منهجية المقال التاريخي والجغرافي"
                                else -> "اليوم الدراسي"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.togglePlannerDay(day) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = { viewModel.togglePlannerDay(day) },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dayText,
                                    fontSize = 13.sp,
                                    color = if (isCompleted) textMuted else textMain,
                                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Footer recommendation / quote
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نصيحة اليوم",
                        fontSize = 11.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"مكاينش شي مستحيل فالباك، شوية د المجهود كل نهار، وحفظ القوالب والعبارات يخليك تجيب نقطة ممتازة!\"",
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: LESONS LIST SCREEN & READER
// ==========================================
@Composable
fun LessonsScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    val subjectState by viewModel.subject.collectAsState()
    var searchTxt by remember { mutableStateOf("") }
    var activeLessonKey by remember { mutableStateOf<String?>(null) } // Key of active read lesson in reader

    // Lessons dataset
    val englishLessons = listOf(
        Triple("tenses", "⏰ Tenses — الأزمنة الكاملة", "شرح 8 أزمنة من البسيط للمستقبل التام مع الكلمات الدلالية وأمثلة الامتحان بالدارجة."),
        Triple("passive", "🔄 Passive Voice — المبني للمجهول", "تصريف جميع الأزمنة في صيغة المبني للمجهول مع شرح حالات التحويل في الامتحان الوطني."),
        Triple("reported-speech", "💭 Reported Speech — الكلام المنقول", "قواعد نقل الجمل الإخبارية، الأوامر، والأسئلة بنوعيها مع جدول التغييرات الزمنية والضمائر."),
        Triple("conditionals", "🌿 Conditionals — الجمل الشرطية واستخدام wish", "شرح تفصيلي للأنواع الأربعة وحالات التمني (Wish / If Only) للتعبير عن الندم."),
        Triple("modals", "⚡ Modals — الأفعال الناقصة", "استعمالات Can, Must, Should في الحاضر والماضي وصياغة Modal Passive في الامتحان."),
        Triple("relative-pronouns", "🔀 Relative Pronouns — ضمائر الوصل", "كيفية دمج الجمل باستخدام Who, Which, Whose, Where والتفرقة بين Defining و Non-defining."),
        Triple("purpose", "🎯 Purpose — الغرض والهدف", "قواعد التعبير عن الغرض الإيجابي والسلبي مع روابط in order to, so as to, so that."),
        Triple("gerund-infinitive", "📌 Gerund & Infinitive — المصدر و ing", "تصنيف الأفعال المتبوعة بالمصدر أو ing، وحالات الأفعال التي تتغير معانيها حسب الصياغة."),
        Triple("linking-words", "🔗 Linking Words — الروابط اللغوية", "روابط الإضافة، التعارض، السبب والنتيجة مع القواعد البنيوية لكل رابط وكيفية ملء الفراغات."),
        Triple("phrasal-verbs", "🧩 Phrasal Verbs — الأفعال المركبة", "جدول شامل يحتوي على 43 فعل مركب الأكثر تكراراً في الامتحانات الوطنية مع معانيها بالدارجة."),
        Triple("functions", "🗣️ Functions — الوظائف اللغوية", "كيفية التعبير عن الرأي، الشكوى، الشك واليقين، الطلب، والاعتذار بطريقة صحيحة ومحاورة.")
    )

    val hgLessons = listOf(
        Triple("hg-overview", "📜 منهجية الاجتماعيات والخرائط", "دليل كامل يشرح الوضعية الأولى والثانية: الاشتغال على وثائق التاريخ أو مقالي الجغرافيا والعكس."),
        Triple("hg-documentary", "📋 منهجية الأسئلة الخمسة للوثائق", "شرح مفصل لطريقة استخراج الإطار التاريخي، تعريف المفاهيم، صياغة الفكرة العامة، وتركيب فقرة تاريخية."),
        Triple("hg-essay", "✍️ منهجية كتابة الموضوع المقالي", "كيفية تصميم المقال: مقدمة جذابة بطرح الإشكاليات، عرض منظم بروابط، وخاتمة تفتح أفاقاً جديدة.")
    )

    val activeList = if (subjectState == "english") englishLessons else hgLessons
    val filteredLessons = activeList.filter {
        it.second.contains(searchTxt, ignoreCase = true) || it.third.contains(searchTxt, ignoreCase = true)
    }

    if (activeLessonKey != null) {
        // Render beautiful native full lesson detail
        val activeKey = activeLessonKey!!
        val lessonContent = LESSONS_CONTENT[activeKey]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header reader bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgCard)
                    .drawBehind {
                        drawLine(
                            color = border,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(16.dp)
            ) {
                IconButton(onClick = { activeLessonKey = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textMain)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lessonContent?.title ?: "تفاصيل الدرس",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )
                    Text(
                        text = lessonContent?.tag ?: "ملخص المادة",
                        fontSize = 11.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scrollable text content area
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // We're converting HTML formatting tags conceptually into sleek beautiful Compose UI chunks
                // Let's render custom native blocks based on parsed tags (conceptual rendering for rich display)
                if (activeKey == "tenses") {
                    TensesLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "passive") {
                    PassiveLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "reported-speech") {
                    ReportedLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "conditionals") {
                    ConditionalsLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "modals") {
                    ModalsLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "relative-pronouns") {
                    RelativeLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "purpose") {
                    PurposeLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "gerund-infinitive") {
                    GerundLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "linking-words") {
                    LinkingLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "phrasal-verbs") {
                    PhrasalLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "functions") {
                    FunctionsLessonReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "hg-overview") {
                    HGOverviewReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "hg-documentary") {
                    HGDocumentaryReader(primaryColor, textMain, textMuted, border)
                } else if (activeKey == "hg-essay") {
                    HGEssayReader(primaryColor, textMain, textMuted, border)
                } else {
                    // Fallback full text
                    Text(
                        text = "تفاصيل الملخص والشروحات المصاحبة مفعّلة في باقي أقسام التطبيق التفاعلية كالبطاقات وحفظ الكلمات وممرن الأفعال.",
                        color = textMuted,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    } else {
        // Main list screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (subjectState == "english") "الدروس والملخصات القواعدية:" else "منهجية التاريخ والجغرافيا:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )
                    IconButton(onClick = { viewModel.startQuiz(subjectState) }) {
                        Icon(Icons.Default.Info, contentDescription = "Quiz", tint = primaryColor)
                    }
                }
            }

            // Simple searchbar inside lessons List
            item {
                OutlinedTextField(
                    value = searchTxt,
                    onValueChange = { searchTxt = it },
                    placeholder = { Text("بحث في محاور المادة...", fontSize = 13.sp, color = textMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textMuted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = border
                    ),
                    singleLine = true
                )
            }

            items(filteredLessons) { lesson ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = bgCard),
                    border = BorderStroke(1.dp, border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeLessonKey = lesson.first }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (lesson.first) {
                                    "tenses" -> "⏰"
                                    "passive" -> "🔄"
                                    "reported-speech" -> "💭"
                                    "conditionals" -> "🌿"
                                    "modals" -> "⚡"
                                    "relative-pronouns" -> "🔀"
                                    "purpose" -> "🎯"
                                    "gerund-infinitive" -> "📌"
                                    "linking-words" -> "🔗"
                                    "phrasal-verbs" -> "🧩"
                                    "functions" -> "🗣️"
                                    "hg-overview" -> "📜"
                                    "hg-documentary" -> "📋"
                                    "hg-essay" -> "✍|"
                                    else -> "📖"
                                },
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lesson.second,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lesson.third,
                                fontSize = 12.sp,
                                color = textMuted,
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Read",
                            tint = textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// LESSON DETAILS CUSTOM READABLE BLOCKS
// ──────────────────────────────────────────
@Composable
fun TensesLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "الأزمنة الثمانية Essential Tenses", primary = primary, textM = textM)
        
        Text("الأزمنة هي مفتاح القواعد في الإنجليزية. إليك شرح كامل لأهم أربعة أزمنة تتكرر بكثرة في الامتحان الوطني:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        LessonSectionDivider(title = "1. Simple Present (الحاضر البسيط)", primary = primary)
        FormulaBox(formula = "Subject + V1 (s/es with he/she/it)")
        Text("• يستعمل للعادات المكررة والحقائق العلمية والميزات الدائمة.\n" +
             "• الكلمات الدلالية: every day, usually, often, always, rarely.", fontSize = 13.sp, color = textMut, lineHeight = 20.sp)
        ExampleBox(text = "He plays football every Monday. / She works in Rabat.")

        LessonSectionDivider(title = "2. Simple Past (الماضي البسيط)", primary = primary)
        FormulaBox(formula = "Subject + V2 (irregular) / V-ed (regular)")
        Text("• يستعمل لفعل انتهى كلياً في وقت محدد فالماضي.\n" +
             "• الكلمات الدلالية: yesterday, ago, last week, in 2023.", fontSize = 13.sp, color = textMut, lineHeight = 20.sp)
        ExampleBox(text = "They visited Fez last year. / I bought a smartphone yesterday.")

        LessonSectionDivider(title = "3. Present Perfect (المضارع التام)", primary = primary)
        FormulaBox(formula = "have / has + Past Participle (V3)")
        Text("• يربط الماضي بالحاضر. يستعمل لحدث وقع قريباً أو له أثر مستمر.\n" +
             "• الكلمات الدلالية: already, yet, just, since, for, ever, never.", fontSize = 13.sp, color = textMut, lineHeight = 20.sp)
        ExampleBox(text = "I have already finished. / She has lived here since 2010.")

        LessonSectionDivider(title = "4. Future Perfect (المستقبل التام)", primary = primary)
        FormulaBox(formula = "will have + Past Participle (V3)")
        Text("• للتعبير عن حدث سيكون قد انتهى كلياً قبل تاريخ أو حدث معين فالمستقبل.\n" +
             "• الكلمات الدلالية: by, by the time, by next week.", fontSize = 13.sp, color = textMut, lineHeight = 20.sp)
        ExampleBox(text = "By 2030, Morocco will have generated 52% of energy from solar power.")
    }
}

@Composable
fun PassiveLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "المبني للمجهول Passive Voice", primary = primary, textM = textM)
        Text("تستعمل صيغة المبني للمجهول لنقل التركيز من الفاعل (من فعل العمل) إلى المفعول به وهو الحدث أو الشيء المتأثر.", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        FormulaBox(formula = "Passive = Object + Be (Conjugated) + V3 (Past Participle)")

        LessonSectionDivider(title = "قاعدة تحويل الأزمنة:", primary = primary)
        tableLayoutHelper(
            rows = listOf(
                "Simple Present" to "is / are + V3",
                "Simple Past" to "was / were + V3",
                "Present Perfect" to "has / have + been + V3",
                "Future Simple" to "will be + V3",
                "Modal Verbs" to "modal + be + V3"
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )

        ExampleBox("Active: They build cars here.\nPassive: Cars are built here.")
        ExampleBox("Active: He wrote a letter yesterday.\nPassive: A letter was written yesterday.")
        ExampleBox("Active: You must follow the rules.\nPassive: The rules must be followed.")
    }
}

@Composable
fun ReportedLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "الكلام المنقول Reported Speech", primary = primary, textM = textM)
        Text("نقل الكلام يتطلب إرجاع الزمن خطوة إلى الماضي وتطابق الضمائر وعبارات الزمان والمكان:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        LessonSectionDivider(title = "جدول تحويل الأزمنة (Backshift)", primary = primary)
        tableLayoutHelper(
            rows = listOf(
                "Simple Present (V1)" to "Simple Past (V2)",
                "Simple Past (V2)" to "Past Perfect (had + V3)",
                "Present Perfect" to "Past Perfect (had + V3)",
                "will" to "would",
                "can / may" to "could / might"
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )

        LessonSectionDivider(title = "تحويل الكلمات الدلالية:", primary = primary)
        tableLayoutHelper(
            rows = listOf(
                "now" to "then",
                "today" to "that day",
                "yesterday" to "the day before / previous day",
                "tomorrow" to "the next day / following day",
                "here / this" to "there / that"
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )

        ExampleBox("Direct: \"I am working now,\" he said.\nReported: He said that he was working then.")
        ExampleBox("Direct: \"Don't shout!\" the teacher said.\nReported: The teacher told us not to shout.")
    }
}

@Composable
fun ConditionalsLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "صيغة الشرط Conditionals & Wish", primary = primary, textM = textM)
        Text("أشهر الأنواع في الامتحان الوطني هما Type 2 و Type 3 بالإضافة لحالات التمني والندم:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        LessonSectionDivider(title = "Conditional Type 2 (حاضر مستحيل/خيالي)", primary = primary)
        FormulaBox(formula = "If + Simple Past (WERE!), would + V1 (base)")
        ExampleBox("If I were you, I would accept the scholarship.\n(نستعمل were بشكل دائم مع جميع الفواعل في التخيل)")

        LessonSectionDivider(title = "Conditional Type 3 (ندم على الماضي)", primary = primary)
        FormulaBox(formula = "If + Past Perfect (had+V3), would have + V3")
        ExampleBox("If she had studied harder last year, she would have passed.")

        LessonSectionDivider(title = "Wish & If Only (التمني والندم)", primary = primary)
        tableLayoutHelper(
            rows = listOf(
                "تمني عن الحاضر" to "I wish + Simple Past\n*مثال:* I wish I spoke French.",
                "ندم عن الماضي" to "I wish + Past Perfect (had+V3)\n*مثال:* I wish I had woken up early."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun ModalsLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "الأفعال الناقصة Modals", primary = primary, textM = textM)
        Text("تستخدم المودالز للتعبير عن الوجوب والاحتمال والنصيحة وتأتي دائماً متبوعة بفعل مصدر مجرد (دون to):", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "Obligation (صيغة وجوب)" to "must / have to\n*مثال:* Students must wear uniform.",
                "Advice (نصيحة)" to "should / ought to / had better\n*مثال:* You should consult a doctor.",
                "Prohibition (منع)" to "mustn't / can't\n*مثال:* You mustn't park here."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun RelativeLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "ضمائر الوصل Relative Pronouns", primary = primary, textM = textM)
        Text("لربط جملتين وتجنب التكرار نستعمل الضمائر الموصولة التالية حسب الاسم الموصوف:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "Who" to "للأشخاص (فاعل)\n*مثال:* The boy who helped me.",
                "Which / That" to "للأشياء والحيوانات\n*مثال:* The book which I read.",
                "Whose" to "للملكية\n*مثال:* The girl whose wallet was stolen.",
                "Where / When" to "للمكان والزمان\n*مثال:* The city where I study."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun PurposeLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "التعبير عن الغرض Purpose", primary = primary, textM = textM)
        Text("للتعبير عن سبب قيامنا بعمل ما (باش / من أجل):", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        FormulaBox(formula = "Positive: to / in order to / so as to + V1")
        ExampleBox("I went to the library to read books.")

        FormulaBox(formula = "Positive clause: so that + subject + can/could + V1")
        ExampleBox("He saved money so that he could buy a laptop.")

        FormulaBox(formula = "Negative: so as not to / in order not to + V1")
        ExampleBox("She left early in order not to miss the train.")
    }
}

@Composable
fun GerundLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "صيغة ing والمصدر Gerund & Infinitive", primary = primary, textM = textM)
        Text("توجد أفعال معينة يجب أن يتلوها الفعل بصيغة ing (Gerund) وأخرى بالمصدر (to + verb):", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "Gerund (verb+ing)" to "enjoy, avoid, suggest, spend time, finish, keep, deny\n*مثال:* I enjoy reading.",
                "Infinitive (to + verb)" to "want, hope, plan, decide, agree, promise, refuse, fail\n*مثال:* She decided to leave."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun LinkingLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "روابط الكلام Linking Words", primary = primary, textM = textM)
        Text("الروابط تضفي مرونة وتنظيماً على موضوعك الإنسشائي وتجعله متماسكاً:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "Addition (إضافة)" to "moreover, furthermore, in addition",
                "Contrast (تعارض)" to "however, although, despite, on the other hand",
                "Cause (سبب)" to "because, since, due to, because of",
                "Result (نتيجة)" to "therefore, as a result, consequently"
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )

        FormulaBox("القاعدة الذهبية:\nalthough + جملة كاملة (S+V)\ndespite + اسم أو verb+ing")
    }
}

@Composable
fun PhrasalLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "الأفعال المركبة Phrasal Verbs", primary = primary, textM = textM)
        Text("أفعال يتغير معناها بالكامل عندما نضيف إليها حرف جر. إليك أشهرها في الامتحان الوطني مترجمة بالدارجة ومرفقة بأمثلة:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "give up" to "يقلع عن / يستسلم\n*مثال:* He gave up smoking.",
                "look after" to "يعتني بـ\n*مثال:* She looks after her sick mother.",
                "bring up" to "يربي الأطفال\n*مثال:* He was brought up in Fez.",
                "carry out" to "ينفذ / يجري تجربة\n*مثال:* To carry out research.",
                "put off" to "يؤجل\n*مثال:* Don't put off your duties."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun FunctionsLessonReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "الوظائف اللغوية Language Functions", primary = primary, textM = textM)
        Text("امتحان الإنجليزية يحتوي دائماً على وضعية حوارية أو مطابقة للوظائف اللغوية:", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "Giving Opinion (الرأي)" to "In my opinion... / I think... / I believe...",
                "Apologizing (الاعتذار)" to "I'm awfully sorry for... / I apologize...",
                "Advising (النصيحة)" to "You should... / If I were you, I would...",
                "Requesting (الطلب)" to "Could you please... / Would you mind..."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun HGOverviewReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "منهجية التاريخ والجغرافيا", primary = primary, textM = textM)
        Text("دليل توضيحي لهيكل توزيع النقاط والوضعيات الاختبارية في الامتحان الوطني لمادة الاجتماعيات (مسلك الآداب والعلوم الإنسانية):", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "الوضعية الأولى (10 نقاط)" to "الاشتغال على الوثائق (تحليل، سياق، أفكار، فقرة)",
                "الوضعية الثانية (10 نقاط)" to "الموضوع المقالي (اختيار موضوع من بين اثنين)"
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )

        Text("• إذا كانت وضعية الوثائق في مادة التاريخ، فإن الموضوع المقالي يكون حتماً في مادة الجغرافيا، والعكس صحيح.", fontSize = 13.sp, color = textMut, lineHeight = 20.sp)
    }
}

@Composable
fun HGDocumentaryReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "منهجية الأسئلة الخمسة للوثائق", primary = primary, textM = textM)
        Text("طريقة الإجابة المنهجية للحصول على علامة كاملة في وضعية الوثائق (التاريخ):", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "1. السياق التاريخي (1ن)" to "تحديد الزمان + المكان + الموضوع العام للوثائق مجتمعة.",
                "2. شرح المفاهيم (1ن)" to "تقديم تعريفات مبسطة وتاريخية للمفاهيم المسطر تحتها.",
                "3. استخراج المعطيات (2ن)" to "استخراج معلومات دقيقة ومباشرة من السندات المطلوبة.",
                "4. الفكرة العامة (2ن)" to "صياغة فكرة محورية موحدة وجامعة لكل الوثائق في سطرين.",
                "5. إنتاج الفقرة (4ن)" to "كتابة فقرة تحليلية مكثفة من المكتسبات القبلية تجيب عن المطلوب."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}

@Composable
fun HGEssayReader(primary: Color, textM: Color, textMut: Color, borderColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LessonBadgeHeader(title = "منهجية كتابة الموضوع المقالي الجغرافي", primary = primary, textM = textM)
        Text("معايير تنقيط الموضوع المقالي (10 نقاط):\n- الجانب المنهجي (مقدمة، عرض، خاتمة): 2ن\n- الجانب المعرفي (انتقاء المعلومات وصحتها): 6ن\n- الجانب الشكلي (تنظيم وخط مقروء): 2ن", fontSize = 14.sp, color = textMut, lineHeight = 22.sp)

        tableLayoutHelper(
            rows = listOf(
                "أ. المقدمة (2 أسطر)" to "توطئة للموضوع + طرح الأسئلة الإشكالية في النهاية.",
                "ب. العرض (فقرات)" to "مناقشة كل سؤال إشكالي في فقرة مستقلة مدعمة بروابط منهجية وأدلة.",
                "ج. الخاتمة (أفق ممتد)" to "خلاصة عامة تركيبية تفتح على تساؤل امتداد يربط بالمحور المقبل."
            ),
            primaryColor = primary,
            borderColor = borderColor,
            textM = textM
        )
    }
}


// Shared widgets for lessons reading templates
@Composable
fun LessonBadgeHeader(title: String, primary: Color, textM: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .border(1.dp, primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LessonSectionDivider(title: String, primary: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(primary, RoundedCornerShape(2.dp))
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = primary
        )
    }
}

@Composable
fun FormulaBox(formula: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = formula,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFF2C94C),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ExampleBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10B981).copy(alpha = 0.04f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.15f)), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = Color(0xFF6EE7B7),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun tableLayoutHelper(
    rows: List<Pair<String, String>>,
    primaryColor: Color,
    borderColor: Color,
    textM: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            Text("القاعدة / التركيب", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryColor, modifier = Modifier.weight(1f))
            Text("المجال والوسائل", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryColor, modifier = Modifier.weight(1f))
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(row.first, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textM, modifier = Modifier.weight(1f))
                Text(row.second, fontSize = 12.sp, color = textM.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            }
            Divider(color = borderColor)
        }
    }
}


// ==========================================
// SCREEN 3: FLASHCARDS (VOCAB STUDY)
// ==========================================
@Composable
fun FlashcardsScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    val currentCardIndex by viewModel.currentSessionIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()
    val activeUnit by viewModel.selectedVocabUnit.collectAsState()
    val unitCards by viewModel.vocabDecksCards.collectAsState()
    val masteredCards by viewModel.masteredFlashcards.collectAsState()

    // Flip transition animations
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "flipCard"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Selector dropdown
        Card(
            colors = CardDefaults.cardColors(containerColor = bgCard),
            border = BorderStroke(1.dp, border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("اختر الوحدة الدراسية للبث:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textMain)
                
                // Select Box
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = primaryColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(activeUnit, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        val units = listOf(
                            "Vocab Unit 1", "Vocab Unit 2", "Vocab Unit 4", 
                            "Vocab Unit 5", "Vocab Unit 6", "Vocab Unit 10"
                        )
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    viewModel.selectVocabUnit(unit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (unitCards.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("جاري تحميل معطيات البطاقات...", color = textMuted)
            }
        } else {
            val activeCard = unitCards[currentCardIndex]
            val reviewKey = "${activeUnit}_${activeCard.front}"
            val isCardSaved = masteredCards.contains(reviewKey)

            // Flashcard UI (card viewport with rotation)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12 * density
                    }
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgCard)
                    .clickable { viewModel.flipCard() }
                    .border(
                        width = 1.dp,
                        color = if (isFlipped) Color(0xFF10B981) else primaryColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .graphicsLayer {
                            // Rotate back text if flipped
                            if (rotation > 90f) {
                                rotationY = 180f
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (rotation <= 90f) {
                        // Front Content
                        Text(
                            text = activeCard.front,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textMain,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "انقر لقلب البطاقة ومعرفة الترجمة 💡",
                            fontSize = 11.sp,
                            color = textMuted,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Back Content
                        Text(
                            text = activeCard.front,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = activeCard.back,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD88F),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "المفردات بالدارجة المغربية",
                            fontSize = 11.sp,
                            color = textMuted
                        )
                    }
                }
            }

            // progress indicator bar
            val progress = (currentCardIndex + 1).toFloat() / unitCards.size
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primaryColor,
                trackColor = border
            )

            // controls panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.prevFlashcard() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(border, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Prev", tint = textMain)
                }

                Button(
                    onClick = { viewModel.toggleMasterFlashcard() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCardSaved) primaryColor else Color.Transparent,
                        contentColor = if (isCardSaved) Color.Black else textMain
                    ),
                    border = BorderStroke(1.dp, border),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(text = if (isCardSaved) "✅" else "➕", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCardSaved) "تم الحفظ فالمذكرات" else "حفظ الكلمة للمراجعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "${currentCardIndex + 1} / ${unitCards.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )

                IconButton(
                    onClick = { viewModel.nextFlashcard() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(border, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Next", tint = textMain)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: IRREGULAR VERBS TABLE + TESTER
// ==========================================
@Composable
fun VerbsScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    val verbsList by viewModel.irregularVerbsList.collectAsState()
    val masteredVerbs by viewModel.masteredVerbs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Testing mode states
    val isTestMode = remember { mutableStateOf(false) }
    val testAnswersPast = remember { mutableMapOf<Int, String>() }
    val testAnswersPp = remember { mutableMapOf<Int, String>() }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ممرن الأفعال الشاذة (105 فعل):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
                Button(
                    onClick = { 
                        isTestMode.value = !isTestMode.value
                        viewModel.playTickSound()
                        testAnswersPast.clear()
                        testAnswersPp.clear()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTestMode.value) primaryColor else primaryColor.copy(alpha = 0.1f),
                        contentColor = if (isTestMode.value) Color.Black else primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isTestMode.value) "الجدول العادي" else "بدء وضع الاختبار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("بحث عن فعل...", fontSize = 13.sp, color = textMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textMuted, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = border
                ),
                singleLine = true
            )
        }

        // Table Rows
        items(verbsList) { verb ->
            val isVerbSaved = masteredVerbs.contains(verb.front)
            val correctPast = verb.back.substringAfter("Past: ").substringBefore("\n")
            val correctPp = verb.back.substringAfter("PP: ").substringBefore("\n")
            val translation = verb.back.substringAfter("Darija: ")

            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = verb.front,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = translation,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD88F),
                            fontFamily = FontFamily.Serif
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isTestMode.value) {
                        // Interactive fields
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var pAnswer by remember(verb.id) { mutableStateOf(testAnswersPast[verb.id] ?: "") }
                            var ppAnswer by remember(verb.id) { mutableStateOf(testAnswersPp[verb.id] ?: "") }

                            val isPCorrect = pAnswer.trim().equals(correctPast.trim(), ignoreCase = true)
                            val isPpCorrect = ppAnswer.trim().equals(correctPp.trim(), ignoreCase = true)

                            // Past Input
                            OutlinedTextField(
                                value = pAnswer,
                                onValueChange = { 
                                    pAnswer = it
                                    testAnswersPast[verb.id] = it
                                },
                                placeholder = { Text("Simple Past", fontSize = 11.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if(pAnswer.isEmpty()) border else if(isPCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                    unfocusedBorderColor = if(pAnswer.isEmpty()) border else if(isPCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            )

                            // PP Input
                            OutlinedTextField(
                                value = ppAnswer,
                                onValueChange = { 
                                    ppAnswer = it
                                    testAnswersPp[verb.id] = it
                                },
                                placeholder = { Text("Past Part.", fontSize = 11.sp, color = textMuted) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if(ppAnswer.isEmpty()) border else if(isPpCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                    unfocusedBorderColor = if(ppAnswer.isEmpty()) border else if(isPpCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            )
                        }
                    } else {
                        // Simple static display of answers
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Simple Past:", fontSize = 10.sp, color = textMuted)
                                Text(correctPast, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Past Participle (PP):", fontSize = 10.sp, color = textMuted)
                                Text(correctPp, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            IconButton(
                                onClick = { viewModel.toggleVerbMastery(verb.front) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = if (isVerbSaved) "✅" else "➕",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 5: WRITING ASSISTANT (ENGLISH)
// ==========================================
@Composable
fun WritingAssistantScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    var selectedType by remember { mutableStateOf("formal") }
    var userDraftText by remember { mutableStateOf("") }
    val data = WRITING_TEMPLATES[selectedType] ?: WRITING_TEMPLATES["formal"]!!

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("مساعد التعبير والإنشاء الكتابي:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textMain)
        }

        // dropdown menu selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("نوع التعبير المطلوب:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textMain)
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = primaryColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(data.title.substringBefore(" ("), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Formal Letter") }, onClick = { selectedType = "formal"; expanded = false })
                            DropdownMenuItem(text = { Text("Informal Letter") }, onClick = { selectedType = "informal"; expanded = false })
                            DropdownMenuItem(text = { Text("Argumentative Essay") }, onClick = { selectedType = "essay"; expanded = false })
                            DropdownMenuItem(text = { Text("Book/Film Review") }, onClick = { selectedType = "review"; expanded = false })
                        }
                    }
                }
            }
        }

        // template layout explanation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(data.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(data.desc, fontSize = 12.sp, color = textMuted, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple template text body representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, border, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = data.tmpl.replace("<strong>", "").replace("</strong>", "").replace("<em>", "").replace("</em>", "").replace("<br>", "\n"),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = textMain,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // writing section box
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("اكتب مسودتك الخاصة هنا:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textMain)
                    Text("الكلمات: ${if(userDraftText.isEmpty()) 0 else userDraftText.trim().split("\\s+".toRegex()).size}", fontSize = 11.sp, color = textMuted)
                }

                OutlinedTextField(
                    value = userDraftText,
                    onValueChange = { userDraftText = it },
                    placeholder = { Text("اكتب وحرر مسودة التعبير مستعينا بالقالب الجانبي...", fontSize = 13.sp, color = textMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = border
                    )
                )

                Button(
                    onClick = {
                        if (userDraftText.isBlank()) {
                            viewModel.playErrorSound()
                            Toast.makeText(context, "المرجو كتابة شيء أولاً لتصديره!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.playVictorySound()
                            // Standard share text intent
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, userDraftText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "تصدير مسودة التعبير")
                            context.startActivity(shareIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصدير ومشاركة الإنشاء", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ==========================================
// SCREEN 6: HG ESSAY CONSTRUCTOR (GEOGRAPHY)
// ==========================================
@Composable
fun CustomConstructorScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    val step by viewModel.constructorStep.collectAsState()
    val text1 by viewModel.constructText1.collectAsState()
    val text2 by viewModel.constructText2.collectAsState()
    val text3 by viewModel.constructText3.collectAsState()
    val text4 by viewModel.constructText4.collectAsState()
    val fullResult by viewModel.assembledHGEssay.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("منشئ ومصمم الموضوع المقالي (الاجتماعيات):", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textMain)
        }

        // Steps indicator progress dots
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (s in 1..4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (s == step) primaryColor else border)
                            .clickable { viewModel.setConstructorStep(s) }
                    )
                }
            }
        }

        // step instruction box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val stepTip = when (step) {
                        1 -> "المقدمة: اكتب تمهيداً عاماً للموضوع (سياق تاريخي أو تعريف جغرافي)، ثم قم بطرح الأسئلة الإشكالية الواردة في نص الموضوع."
                        2 -> "العرض الجزء الأول: أجب عن السؤال الإشكالي الأول مستخدماً أسلوب التحليل التاريخي أو الوصف الجغرافي. استعمل روابط مناسبة."
                        3 -> "العرض الجزء الثاني: أجب عن السؤال الإشكالي الثاني. ركز على الترتيب الزمني للأحداث أو العوامل الجغرافية المفسرة."
                        else -> "الخاتمة: اكتب خلاصة واستنتاجاً عاماً يفتح أفاقاً جديدة (سؤال امتداد) يربط بما سيأتي."
                    }
                    Text("خطوات التحرير:", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stepTip, fontSize = 13.sp, color = textMain, lineHeight = 20.sp)
                }
            }
        }

        // phrase helpers box
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("المفاتيح والعبارات المنهجية الجاهزة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textMain)
                
                val phrases = when (step) {
                    1 -> listOf(
                        "شهد العالم المعاصر خلال القرن العشرين تحولات سياسية واقتصادية عميقة تمثلت في... ",
                        "تعتبر الولايات المتحدة الأمريكية قوة اقتصادية عظمى تهيمن على المجالات العالمية... ",
                        "تميزت فترة ما بين الحربين بظهور أزمات اقتصادية وهزات سياسية أثرت على... ",
                        "فما هي مظاهر وتجليات هذا التحول؟ وما هي العوامل المفسرة له؟ وما هي المشاكل المترتبة؟"
                    )
                    2 -> listOf(
                        "تتجلى مظاهر وتجليات هذه الظاهرة في مستويات متعددة، أولها... ",
                        "على المستوى التاريخي، انطلق الحدث مع اندلاع أزمة... ",
                        "ارتباطاً بذلك، ساهمت مجموعة من التحولات الهيكلية في تسريع... "
                    )
                    3 -> listOf(
                        "لتفسير هذه التجليات، يمكن إرجاع العوامل الرئيسية إلى... ",
                        "بالإضافة إلى ذلك، لعبت العوامل الطبيعية والبشرية دوراً حاسماً في... ",
                        "من زاوية أخرى، شكل التدخل التنظيمي للدولة محركاً أساسياً لـ... "
                    )
                    else -> listOf(
                        "تأسيساً على ما سبق، يمكن القول إن التحولات التي شهدها الموضوع تدل على... ",
                        "ختاماً، يمكن استخلاص أن هذه القوة الاقتصادية تواجه تحديات حقيقية تفرض عليها... ",
                        "وبناءً على هذه التطورات، يطرح التساؤل حول مدى قدرة المنظومة على الصمود أمام... "
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    phrases.forEach { phrase ->
                        Button(
                            onClick = { viewModel.insertPhraseToConstructor(phrase) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = textMain),
                            border = BorderStroke(1.dp, border),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(phrase, fontSize = 11.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        // text inputs edit field
        item {
            val cellValue = when(step) {
                1 -> text1
                2 -> text2
                3 -> text3
                else -> text4
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when(step) {
                        1 -> "محرر المقدمة الإشكالية:"
                        2 -> "محرر العرض - المحور الأول:"
                        3 -> "محرر العرض - المحور الثاني:"
                        else -> "محرر الخاتمة والتركيب:"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )

                OutlinedTextField(
                    value = cellValue,
                    onValueChange = {
                        viewModel.playTickSound()
                        when(step) {
                            1 -> viewModel.constructText1.value = it
                            2 -> viewModel.constructText2.value = it
                            3 -> viewModel.constructText3.value = it
                            4 -> viewModel.constructText4.value = it
                        }
                    },
                    placeholder = { Text("اكتب فقرة كاستجابة للسؤال المقالي...", fontSize = 13.sp, color = textMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = border
                    )
                )
            }
        }

        // flow indicators prev/next button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if(step > 1) viewModel.setConstructorStep(step - 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = border, contentColor = textMain),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("السابق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { if(step < 4) viewModel.setConstructorStep(step + 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = border, contentColor = textMain),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("التالي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = { viewModel.assembleHGEssay() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تركيب المقال النهائي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Final result assembled display
        if (fullResult.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = bgCard),
                    border = BorderStroke(1.dp, border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("المقال النهائي المركب:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            IconButton(
                                onClick = {
                                    // standard share text intent
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, fullResult)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة المقال الجغرافي النهائي")
                                    context.startActivity(shareIntent)
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = primaryColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = fullResult,
                                fontSize = 13.sp,
                                color = textMain,
                                lineHeight = 20.sp,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 7: PLAY QUIZ ARENA
// ==========================================
@Composable
fun QuizHubScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    val qType by viewModel.activeQuizType.collectAsState()
    val questions by viewModel.quizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val isFinished by viewModel.quizFinished.collectAsState()
    val selectedItem by viewModel.selectedQuizAnswerIdx.collectAsState()

    if (questions.isEmpty()) {
        viewModel.startQuiz("english") // Launch english by default if empty
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("مسابقات ذكية واختبارات منهجية:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.startQuiz("english") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(qType == "english") primaryColor else Color.Transparent,
                        contentColor = if(qType == "english") Color.Black else textMain
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, border)
                ) {
                    Text("الإنجليزية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.startQuiz("hg") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(qType == "hg") primaryColor else Color.Transparent,
                        contentColor = if(qType == "hg") Color.White else textMain
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, border)
                ) {
                    Text("الاجتماعيات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isFinished) {
            // Trophy display celebration
            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Trophy",
                        tint = primaryColor,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "النتيجة النهائية للمسابقة:",
                        fontSize = 14.sp,
                        color = textMuted
                    )
                    Text(
                        text = "$score / ${questions.size * 2}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = textMain
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val feedbackMsg = if (score >= (questions.size * 2) * 0.8) "أداء ممتاز! Falcon Champ ⚡ أنت جاهز للامتحان الوطني"
                                      else if (score >= (questions.size * 2) * 0.5) "أداء جيد جداً! واصل المراجعة والتحضير"
                                      else "تحتاج إلى مراجعة بعض الثغرات وإعادة المحاولة."
                    Text(
                        text = feedbackMsg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.startQuiz(qType) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة المسابقة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (questions.isNotEmpty()) {
            val q = questions[currentIndex]

            // Question Box
            Card(
                colors = CardDefaults.cardColors(containerColor = bgCard),
                border = BorderStroke(1.dp, border),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("السؤال ${currentIndex + 1} من ${questions.size}", fontSize = 11.sp, color = textMuted)
                        Text("النقاط: $score", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = q.q,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Multi choice fields
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        q.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedItem == idx
                            val isCorrectIdx = idx == q.correctIdx
                            val optionBgColor = if (selectedItem == null) {
                                Color.White.copy(alpha = 0.03f)
                            } else if (isCorrectIdx) {
                                Color(0xFF10B981).copy(alpha = 0.15f) // Green correct indicator
                            } else if (isSelected) {
                                Color(0xFFEF4444).copy(alpha = 0.15f) // Red wrong indicator
                            } else {
                                Color.White.copy(alpha = 0.01f)
                            }
                            val optionBorderColor = if (selectedItem == null) {
                                border
                            } else if (isCorrectIdx) {
                                Color(0xFF10B981)
                            } else if (isSelected) {
                                Color(0xFFEF4444)
                            } else {
                                border.copy(alpha = 0.5f)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(optionBgColor, RoundedCornerShape(10.dp))
                                    .border(1.dp, optionBorderColor, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectQuizAnswer(idx) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(border, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(idx) {
                                            0 -> "A"
                                            1 -> "B"
                                            2 -> "C"
                                            else -> "D"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textMain
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = opt,
                                    fontSize = 13.sp,
                                    color = textMain,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Explanation Box
                    if (selectedItem != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "الشرح التوضيحي بالدارجة:\n${q.ex}",
                                fontSize = 12.sp,
                                color = textMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 8: REVIEW DUE SESSION (SM-2 WORKFLOW)
// ==========================================
@Composable
fun ReviewSessionScreen(
    viewModel: StudyViewModel,
    primaryColor: Color,
    textMain: Color,
    textMuted: Color,
    bgCard: Color,
    border: Color
) {
    val queue by viewModel.studySessionQueue.collectAsState()
    val currentIndex by viewModel.currentSessionIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()

    // Flip transition animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "flipCardReview"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Queue Progress Indicator
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("مراجعة البطاقات المستحقة (SM-2):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
            Text("${currentIndex + 1} / ${queue.size}", fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.Bold)
        }

        if (queue.isNotEmpty() && currentIndex < queue.size) {
            val card = queue[currentIndex]

            // Rotating flashcard
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12 * density
                    }
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgCard)
                    .clickable { viewModel.flipCard() }
                    .border(
                        width = 1.dp,
                        color = if (isFlipped) Color(0xFF10B981) else primaryColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .graphicsLayer {
                            // Rotate back text if flipped
                            if (rotation > 90f) {
                                rotationY = 180f
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (rotation <= 90f) {
                        // Front
                        Text(
                            text = card.front,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textMain,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.hint,
                            fontSize = 12.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "انقر لقلب البطاقة ومعرفة الترجمة 💡",
                            fontSize = 11.sp,
                            color = textMuted,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Back
                        Text(
                            text = card.back,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD88F),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            // progress bar
            val progress = (currentIndex + 1).toFloat() / queue.size
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primaryColor,
                trackColor = border
            )

            // review scoring controllers
            if (isFlipped) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ما مدى سهولة استرجاع هذه الكلمة؟", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textMuted)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hard
                        Button(
                            onClick = { viewModel.rateActiveCard(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("صعبة جداً", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        
                        // Good
                        Button(
                            onClick = { viewModel.rateActiveCard(3) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("مقبولة", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                        // Easy
                        Button(
                            onClick = { viewModel.rateActiveCard(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("سهلة جداً", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.flipCard() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("أظهر الجواب والترجمة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// Static data container matching WRITING_TEMPLATES exactly
class WritingTemplateData(val title: String, val desc: String, val tmpl: String)

val WRITING_TEMPLATES = mapOf(
    "formal" to WritingTemplateData(
        title = "Formal Letter / Email",
        desc = "رسالة إدارية رسمية (طلب عمل، شكوى، طلب معلومات). استخدم لغة مهذبة وخالية من الاختصارات.",
        tmpl = """<strong>[Your Address]</strong><br>
<strong>[Recipient Name / Company]</strong><br>
<strong>[Date]</strong><br><br>
Dear Sir or Madam, <em>(أو Dear Mr. Smith)</em><br><br>
I am writing to you in order to <strong>[state purpose: e.g. complain about... / apply for the post of...]</strong>.<br><br>
First of all, I would like to point out that <strong>[Reason 1]</strong>. In addition, <strong>[Reason 2]</strong>. Furthermore, it is worth mentioning that <strong>[Reason 3]</strong>.<br><br>
For these reasons, I would be grateful if you could <strong>[state request: e.g. look into this matter / grant me an interview]</strong>.<br><br>
Thank you for your time and consideration. I look forward to hearing from you at your earliest convenience.<br><br>
Yours faithfully, <em>(إذا بدأت بـ Dear Sir/Madam)</em><br>
Yours sincerely, <em>(إذا بدأت بالاسم Dear Mr...)</em><br>
<strong>[Your Name]</strong>"""
    ),
    "informal" to WritingTemplateData(
        title = "Informal Email / Letter",
        desc = "رسالة ودية لصديق أو قريب. يمكنك استعمال عبارات شائعة واختصارات.",
        tmpl = """Dear <strong>[Friend's Name]</strong>,<br><br>
How are you doing? I hope this letter finds you in good health and high spirits. I am writing this to tell you all about <strong>[Topic]</strong>.<br><br>
You won't believe what happened! To start with, <strong>[Point 1]</strong>. Besides, <strong>[Point 2]</strong>. Guess what? <strong>[Point 3]</strong>.<br><br>
Anyway, I must stop writing now because I have to study. Give my regards to your family. I can't wait to hear your news!<br><br>
Write back soon,<br>
Best wishes / Warm regards,<br>
<strong>[Your Name]</strong>"""
    ),
    "essay" to WritingTemplateData(
        title = "Argumentative Essay (For & Against)",
        desc = "موضوع إنشائي يعرض وجهتي نظر مؤيدة ومعارضة لموضوع معين بشكل محايد ومتوازن.",
        tmpl = """Nowadays, the issue of <strong>[Topic]</strong> has raised a lot of debate among people. While some argue that <strong>[Opinion For]</strong>, others believe that <strong>[Opinion Against]</strong>. In this essay, I will discuss both points of view.<br><br>
On the one hand, supporters of this idea claim that it has several benefits. To begin with, <strong>[Advantage 1]</strong>. Another point is that <strong>[Advantage 2]</strong>. For instance, <strong>[Example]</strong>.<br><br>
On the other hand, opponents point out some drawbacks. First of all, <strong>[Disadvantage 1]</strong>. Moreover, <strong>[Disadvantage 2]</strong>. As a result, <strong>[Consequence]</strong>.<br><br>
To sum up, and taking everything into consideration, I believe that despite the disadvantages of <strong>[Topic]</strong>, its advantages are far more significant. We should try to use it wisely."""
    ),
    "review" to WritingTemplateData(
        title = "Book / Film Review",
        desc = "مراجعة كتاب أو فيلم. تتضمن ملخص الحبكة، الشخصيات، التقييم والتوصية النهائية.",
        tmpl = """Recently, I read an interesting book titled <strong>"[Book Title]"</strong> written by the famous author <strong>[Author]</strong>.<br><br>
The story is set in <strong>[Setting]</strong> and centers around <strong>[Main Character]</strong>, who <strong>[brief plot summary: e.g. embarks on a journey to find...]</strong>.<br><br>
What I liked most about this book is the way the author developed the characters. In addition, the plot is full of suspense and keeps the reader hooked. However, the ending was somewhat predictable.<br><br>
All in all, I highly recommend this book to anyone who loves <strong>[Genre]</strong>. It is definitely a masterpiece that deserves a 5-star rating."""
    )
)
