package com.example.ui

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClipboardEntry
import com.example.data.ShortcutPhrase
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- REUSABLE GLASS CARD COMPONENT ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderGlass,
    borderWidth: Dp = 1.dp,
    containerColor: Color = CosmicDarkCard.copy(alpha = 0.82f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

// --- AMBIENT GLOW BACKDROP PATTERN ---
@Composable
fun AmbientBlurBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x1"
    )
    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicSlateBg)
            .drawBehind {
                // Drawing cyber ambient radiant orbs
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MysticalPurple.copy(alpha = 0.22f), Color.Transparent),
                        radius = size.width * 0.7f
                    ),
                    center = this.center.copy(
                        x = this.center.x + animOffset1,
                        y = this.center.y + animOffset2
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.12f), Color.Transparent),
                        radius = size.width * 0.5f
                    ),
                    center = this.center.copy(
                        x = this.center.x - animOffset2,
                        y = this.center.y - animOffset1
                    )
                )
            }
    ) {
        content()
    }
}

// --- SCREEN 1: WELCOME & ONBOARDING ---
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    var nameInput by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("Futuristic Glassmorphic") }
    var selectedLang by remember { mutableStateOf("English") }

    val themes = listOf("Glassmorphism Dark", "Futuristic Neon", "Sleek Gboard Lite")
    val languages = listOf("English", "Hinglish", "Hindi", "Spanish", "French")

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // AI pulsing glow icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.linearGradient(listOf(ElectricBlue, NeonPurple)),
                        CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CosmicSlateBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "App Logo",
                        tint = ElectricBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "KEYASSIST AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = ElectricBlue,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Type Smarter. Write Faster. Express Better.",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Welcome onboard! Tell us about your preferences.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Name Input
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("What should we call you?", color = TextSecondary) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderGlass,
                        focusedLabelColor = ElectricBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select Keyboard Theme Style
                Text("Select Keyboard Design", fontSize = 13.sp, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { style ->
                        val isSelected = selectedTheme == style
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) ElectricBlue.copy(alpha = 0.25f) else CosmicDarkCard,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricBlue else BorderGlass,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTheme = style }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(style, fontSize = 12.sp, color = if (isSelected) ElectricBlue else TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selection
                Text("Primary Input Language", fontSize = 13.sp, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedLang == lang
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NeonPurple.copy(alpha = 0.25f) else CosmicDarkCard,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPurple else BorderGlass,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedLang = lang }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(lang, fontSize = 12.sp, color = if (isSelected) NeonPurple else TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.finishOnboarding(nameInput, selectedTheme, selectedLang)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "LAUNCH ENGINE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- SCREEN 2: MAIN HUB / DASHBOARD ---
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUser.collectAsStateWithLifecycle()
    val stats by viewModel.typingInsights.collectAsStateWithLifecycle()

    val wordCount = stats["words_typed"] ?: 0
    val errorsFixed = stats["errors_fixed"] ?: 0
    val timeSaved = stats["time_saved_mins"] ?: 0

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Premium header banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hello, $name 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Your AI engine is fully calibrated.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                // Premium Badge indicator
                Box(
                    modifier = Modifier
                        .background(
                            if (isPremium) NeonPurple.copy(alpha = 0.2f) else CosmicDarkCard,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isPremium) NeonPurple else BorderGlass,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.navigateTo(MainViewModel.Screen.PremiumCenter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Subscription badge",
                            tint = if (isPremium) NeonPurple else ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPremium) "PREMIUM" else "GO PRO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) NeonPurple else ElectricBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Dashboard Stats banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$wordCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        Text("Words Typed", fontSize = 11.sp, color = TextSecondary)
                    }
                    Box(modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(BorderGlass))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$errorsFixed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                        Text("Grammar Fixed", fontSize = 11.sp, color = TextSecondary)
                    }
                    Box(modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(BorderGlass))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${timeSaved}m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Time Saved", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "KEYBOARD HUB",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid Layout for Screens
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Sandbox Trigger (Primary highlight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(IntenseBlue.copy(alpha = 0.2f), CosmicDarkCard)),
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, DynamicBorderBrush(), RoundedCornerShape(16.dp))
                        .clickable { viewModel.navigateTo(MainViewModel.Screen.KeyboardSimulator) }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Chat, "Sandbox", tint = ElectricBlue, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Interactive Chat Sandbox", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Test Gboard layout, smart replies, Hinglish", fontSize = 12.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, "Enter", tint = ElectricBlue)
                }

                // Grid cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assignment,
                        title = "Clipboard",
                        desc = "Saved clipboards",
                        accentColor = ElectricBlue,
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.ClipboardHistory) }
                    )
                    DashboardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.OfflineBolt,
                        title = "Shortcuts",
                        desc = "Phrase expanders",
                        accentColor = NeonPurple,
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.ShortcutsManager) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BarChart,
                        title = "Insights",
                        desc = "Daily productivity",
                        accentColor = Color.White,
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.TypingInsights) }
                    )
                    DashboardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Translate,
                        title = "Languages",
                        desc = "Active translation",
                        accentColor = ElectricBlue,
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.LanguageConfig) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Mic,
                        title = "Voice Typing",
                        desc = "Acoustic speechpad",
                        accentColor = NeonPurple,
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.VoiceHelper) }
                    )
                    DashboardButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        desc = "Preferences",
                        accentColor = TextSecondary,
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.Settings) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun DynamicBorderBrush(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val translationVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    return Brush.linearGradient(
        colors = listOf(ElectricBlue, NeonPurple.copy(alpha = 0.5f), ElectricBlue),
    )
}

@Composable
fun DashboardButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    desc: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(CosmicDarkCard.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(desc, fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
        }
    }
}

// --- SCREEN 3: KEYBOARD INTERFACE / CHAT SIMULATOR SANDBOX ---
@Composable
fun KeyboardSimulatorScreen(viewModel: MainViewModel) {
    val currentInput by viewModel.keyboardInputBuffer.collectAsStateWithLifecycle()
    val chatPartner by viewModel.activeChatPartnerName.collectAsStateWithLifecycle()
    val partnerColor by viewModel.activeChatPartnerAvatarColor.collectAsStateWithLifecycle()
    val chatHistory by viewModel.simulatedChatHistory.collectAsStateWithLifecycle()
    val aiSuggestions by viewModel.aiSuggestions.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val apiError by viewModel.apiErrorMessage.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll chat to bottom when message is added
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Simulated Phone Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }

                // Partner Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(partnerColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chatPartner.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(chatPartner, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("WhatsApp Mockup • Online", fontSize = 11.sp, color = ElectricBlue)
                }

                // Toggle Partner Dropdown Menu
                IconButton(onClick = {
                    val next = when (chatPartner) {
                        "Sarah (Boss)" -> "Rohan (Friend)"
                        "Rohan (Friend)" -> "Alex (Client)"
                        else -> "Sarah (Boss)"
                    }
                    viewModel.resetChatSimulator(next)
                }) {
                    Icon(Icons.Default.SwapCalls, "Switch chat", tint = ElectricBlue)
                }
            }

            // Simulated Chat Log
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(chatHistory) { msg ->
                    ChatBubble(msg = msg)
                }
            }

            // --- THE CYBERPUNK KEYBOARD OVERLAY (MODERN APP GBOARD MIX) ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    
                    // 1. FLOATING AI SUGGESTION BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0E17))
                            .border(BorderStroke(1.dp, BorderGlass))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Panel",
                            tint = NeonPurple,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 2.dp)
                        )

                        if (aiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 4.dp),
                                color = ElectricBlue,
                                strokeWidth = 2.dp
                            )
                        }

                        // Smart suggestion chips scrolling horizontally
                        LazyRow(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(aiSuggestions) { reply ->
                                Box(
                                    modifier = Modifier
                                        .background(IntenseBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { viewModel.insertTemplate(reply) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(reply, fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Shortcut quick actions (Grammer, Hinglish, Volume, Clipboard, etc.)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { viewModel.triggerGrammarCorrection() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Spellcheck, "Fix Grammar", tint = Color.Green, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.triggerHinglishToEnglish() }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Translate, "Hinglish convert", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.isTonePopupVisible.value = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.FormatQuote, "Tone Changer", tint = NeonPurple, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (apiError.isNotEmpty()) {
                        Text(
                            text = apiError,
                            color = Color.Red,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // 2. PRIMARY CHAT ENTRY DISPLAY PANEL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CosmicDarkCard, RoundedCornerShape(14.dp))
                                .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            if (currentInput.isEmpty()) {
                                Text("Select text tool or start typing standard QWERTY...", color = TextMuted, fontSize = 13.sp)
                            } else {
                                Text(currentInput, color = TextPrimary, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ElectricBlue, CircleShape)
                                .clickable { viewModel.sendSimulatedMessage() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }

                    // --- OVERLAY WINDOW INJECTIONS ---
                    AnimatedVisibility(
                        visible = viewModel.isTonePopupVisible.collectAsStateWithLifecycle().value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        ToneRewritePopupOverlay(viewModel)
                    }

                    AnimatedVisibility(
                        visible = viewModel.isClipboardPanelVisible.collectAsStateWithLifecycle().value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        ClipboardKeyboardOverlay(viewModel)
                    }

                    AnimatedVisibility(
                        visible = viewModel.isVoiceOverlayVisible.collectAsStateWithLifecycle().value,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        VoiceKeyboardOverlay(viewModel)
                    }

                    // 3. PHYSICAL KEYBOARD BUTTONS CHASSIS
                    if (!viewModel.isTonePopupVisible.collectAsStateWithLifecycle().value &&
                        !viewModel.isClipboardPanelVisible.collectAsStateWithLifecycle().value &&
                        !viewModel.isVoiceOverlayVisible.collectAsStateWithLifecycle().value
                    ) {
                        QwerkyHardwareLayout(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: MainViewModel.ChatMessage) {
    val alignment = if (msg.isMe) Alignment.End else Alignment.Start
    val containerColor = if (msg.isMe) IntenseBlue else CosmicDarkCard
    val textColor = if (msg.isMe) Color.White else TextPrimary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (msg.isMe) 16.dp else 2.dp,
                        bottomEnd = if (msg.isMe) 2.dp else 16.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (msg.isMe) Color.Transparent else BorderGlass,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (msg.isMe) 16.dp else 2.dp,
                        bottomEnd = if (msg.isMe) 2.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                text = msg.text,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// QWERTY Soft keyboard layout renderer
@Composable
fun QwerkyHardwareLayout(viewModel: MainViewModel) {
    val hapticEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val buffer by viewModel.keyboardInputBuffer.collectAsStateWithLifecycle()

    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    val row3 = listOf("Z", "X", "C", "V", "B", "N", "M")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10121A))
            .padding(top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { letter ->
                KeyButton(title = letter, weight = 1f) {
                    viewModel.keyboardInputBuffer.value = buffer + letter.lowercase()
                }
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { letter ->
                KeyButton(title = letter, weight = 1f) {
                    viewModel.keyboardInputBuffer.value = buffer + letter.lowercase()
                }
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift spacer / icon
            KeyButton(icon = Icons.Default.VerticalAlignTop, weight = 1.2f) {
                // Toggle uppercase preview is mock
            }

            row3.forEach { letter ->
                KeyButton(title = letter, weight = 1f) {
                    viewModel.keyboardInputBuffer.value = buffer + letter.lowercase()
                }
            }

            // Backspace Key
            KeyButton(icon = Icons.Default.Backspace, weight = 1.5f, tint = Color.Red) {
                if (buffer.isNotEmpty()) {
                    viewModel.keyboardInputBuffer.value = buffer.substring(0, buffer.length - 1)
                }
            }
        }

        // Row 4 (Space panel)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tone popup key
            KeyButton(icon = Icons.Default.Face, weight = 1.2f) {
                viewModel.isTonePopupVisible.value = true
            }

            // Clipboard overlay key
            KeyButton(icon = Icons.Default.ContentPaste, weight = 1.2f) {
                viewModel.isClipboardPanelVisible.value = true
            }

            // SPACE BAR
            Box(
                modifier = Modifier
                    .weight(5f)
                    .height(44.dp)
                    .background(KeyboardKeyBg, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.keyboardInputBuffer.value = buffer + " " }
                )
                Text("SPACE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            }

            // Speech dictation key
            KeyButton(icon = Icons.Default.Mic, weight = 1.2f) {
                viewModel.isVoiceOverlayVisible.value = true
                viewModel.startVoiceTyping()
            }
        }
    }
}

@Composable
fun RowScope.KeyButton(
    title: String? = null,
    icon: ImageVector? = null,
    weight: Float,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .background(KeyboardKeyBg, RoundedCornerShape(8.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (title != null) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tint)
        } else if (icon != null) {
            Icon(icon, "key", tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

// --- SCREEN 4: TONE REWRITE OVERLAY CARD ---
@Composable
fun ToneRewritePopupOverlay(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentText by viewModel.keyboardInputBuffer.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F121F))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rewrite Current Phrase Tone", fontWeight = FontWeight.Bold, color = NeonPurple, fontSize = 14.sp)
            IconButton(onClick = { viewModel.isTonePopupVisible.value = false }) {
                Icon(Icons.Default.Close, "Close", tint = TextPrimary)
            }
        }

        Text(
            text = "Currently rewriting: \"${currentText.ifEmpty { "(Your typing buffer is empty)" }}\"",
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 2,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tone button builder
            ToneSelectorCard(
                title = "Professional 💼",
                desc = "Corporate standard",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.triggerToneRewrite("Professional")
            }

            ToneSelectorCard(
                title = "Friendly 👋",
                desc = "Warm & emoji rich",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.triggerToneRewrite("Friendly")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToneSelectorCard(
                title = "Formal 🎖️",
                desc = "Diplomatic etiquette",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.triggerToneRewrite("Formal")
            }

            ToneSelectorCard(
                title = "Funny 🤪",
                desc = "Witty sarcasm",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.triggerToneRewrite("Funny")
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun ToneSelectorCard(
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(CosmicDarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Text(desc, fontSize = 9.sp, color = TextSecondary)
    }
}

// --- SCREEN 5: CLIPBOARD MANAGER (Dedicated screen & keyboard overlay) ---
@Composable
fun ClipboardKeyboardOverlay(viewModel: MainViewModel) {
    val clipboards by viewModel.clipboardHistory.collectAsStateWithLifecycle()
    val buffer by viewModel.keyboardInputBuffer.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(0xFF0F121F))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentPaste, "Clip", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clipboard History (Tap to paste)", fontWeight = FontWeight.Bold, color = ElectricBlue, fontSize = 13.sp)
            }
            Row {
                IconButton(onClick = { viewModel.clearClipboard() }) {
                    Icon(Icons.Default.DeleteForever, "Clear", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { viewModel.isClipboardPanelVisible.value = false }) {
                    Icon(Icons.Default.Close, "Close", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        if (clipboards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your clipboard is empty.", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(clipboards) { clip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicDarkCard, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.keyboardInputBuffer.value = buffer + " " + clip.text
                                viewModel.isClipboardPanelVisible.value = false
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clip.text,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.deleteClipboardItem(clip.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClipboardHistoryScreen(viewModel: MainViewModel) {
    val clipboards by viewModel.clipboardHistory.collectAsStateWithLifecycle()
    var clipboardText by remember { mutableStateOf("") }

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Clipboard Manager", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Quick Save Clipboard Snippet", fontWeight = FontWeight.Bold, color = ElectricBlue)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = clipboardText,
                        onValueChange = { clipboardText = it },
                        placeholder = { Text("Enter text to store...", color = TextSecondary) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = BorderGlass),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addClipboardText(clipboardText)
                            clipboardText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add", tint = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Logs", fontWeight = FontWeight.Bold, color = TextPrimary)
                TextButton(onClick = { viewModel.clearClipboard() }) {
                    Text("Clear All", color = Color.Red)
                }
            }

            if (clipboards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No clips saved. Anything you copy inside Keyboard Sandbox will sync here!", color = TextSecondary, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clipboards) { clip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicDarkCard, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(clip.text, fontSize = 14.sp, color = TextPrimary)
                            }
                            IconButton(onClick = { viewModel.deleteClipboardItem(clip.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: SETTINGS SCREEN & SHORTCUTS MANAGER ---
@Composable
fun ShortcutsManagerScreen(viewModel: MainViewModel) {
    val shortcuts by viewModel.shortcutPhrases.collectAsStateWithLifecycle()
    var keyword by remember { mutableStateOf("") }
    var actionText by remember { mutableStateOf("") }

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Custom Shortcuts", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Register Shortcut Expanders", fontWeight = FontWeight.Bold, color = NeonPurple)
                Text("Typing keyword in chat converts it to the full phrase automatically!", fontSize = 10.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        label = { Text("If I type...", color = TextSecondary) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = BorderGlass),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = actionText,
                        onValueChange = { actionText = it },
                        label = { Text("Replace with", color = TextSecondary) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple, unfocusedBorderColor = BorderGlass),
                        modifier = Modifier.weight(1.8f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.addShortcutPhrase(keyword, actionText)
                        keyword = ""
                        actionText = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ADD SHORTCUT MATCH", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Active Expansions", fontWeight = FontWeight.Bold, color = TextPrimary)

            if (shortcuts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No custom shortcuts mapped.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp)
                ) {
                    items(shortcuts) { shortcut ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicDarkCard, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(shortcut.shortcut, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                                Text(shortcut.expandedText, fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { viewModel.deleteShortcutPhrase(shortcut.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: TYPING INSIGHTS (Charts Dashboard) ---
@Composable
fun TypingInsightsScreen(viewModel: MainViewModel) {
    val stats by viewModel.typingInsights.collectAsStateWithLifecycle()

    val totalWords = stats["words_typed"] ?: 0
    val grammarFixed = stats["errors_fixed"] ?: 0
    val shortcutsUsed = stats["custom_shortcuts_used"] ?: 0
    val totalTime = stats["time_saved_mins"] ?: 0

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Typing Productivity", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(20.dp))

            // Main dashboard summary card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(IntenseBlue, MysticalPurple)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text("PRODUCTIVITY FACTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                    Text("$totalTime Mins Saved", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("AI-assisted writing results this week.", fontSize = 12.sp, color = TextPrimary.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("CORE PERFORMANCE QUANTIFIERS", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

            // Performance item list
            QuantifierRow(title = "Total words typed", value = "$totalWords words", icon = Icons.Default.Chat, color = ElectricBlue)
            QuantifierRow(title = "Grammar corrections processed", value = "$grammarFixed issues", icon = Icons.Default.CheckCircle, color = Color.Green)
            QuantifierRow(title = "Shortcuts automatically expanded", value = "$shortcutsUsed terms", icon = Icons.Default.OfflineBolt, color = NeonPurple)

            Spacer(modifier = Modifier.height(20.dp))

            // AI analysis statement
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "AI suggestion", tint = ElectricBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AI INSIGHT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElectricBlue)
                        Text(
                            "You are currently typing 35% faster than your past baseline due to active Hinglish-to-English rewrite models. Keep deploying shortcuts to optimize workflow.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuantifierRow(title: String, value: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(CosmicDarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// --- SCREEN 8: LANGUAGE SELECTION & DICTIONARY SETTINGS ---
@Composable
fun LanguageConfigScreen(viewModel: MainViewModel) {
    val languages = listOf(
        "English (US / UK)",
        "Hinglish (Hindi written in Roman characters)",
        "Hindi (Original Devanagari script)",
        "Spanish (Español)",
        "French (Français)"
    )
    val chosenLang by viewModel.preferredLanguage.collectAsStateWithLifecycle()

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Languages & Keyboards", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Configure AI Language Engine", fontWeight = FontWeight.Bold, color = ElectricBlue)
                Text("KeyAssist uses dedicated large language models parsed for specific cultural slangs.", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("SUPPORTED VOCABULARIES", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(languages) { lang ->
                    val isChecked = chosenLang.lowercase() in lang.lowercase() || (lang.startsWith("Hinglish") && chosenLang == "Hinglish")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicDarkCard, RoundedCornerShape(14.dp))
                            .border(1.dp, if (isChecked) ElectricBlue else BorderGlass, RoundedCornerShape(14.dp))
                            .clickable {
                                val shortName = when {
                                    lang.contains("Hinglish") -> "Hinglish"
                                    lang.contains("Hindi") -> "Hindi"
                                    lang.contains("Spanish") -> "Spanish"
                                    lang.contains("French") -> "French"
                                    else -> "English"
                                }
                                viewModel.preferredLanguage.value = shortName
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(lang, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                            Text(if (lang.contains("Hinglish")) "Optimized translation models active" else "Standard dictionaries available", fontSize = 10.sp, color = TextSecondary)
                        }

                        RadioButton(
                            selected = isChecked,
                            onClick = {
                                val shortName = when {
                                    lang.contains("Hinglish") -> "Hinglish"
                                    lang.contains("Hindi") -> "Hindi"
                                    lang.contains("Spanish") -> "Spanish"
                                    lang.contains("French") -> "French"
                                    else -> "English"
                                }
                                viewModel.preferredLanguage.value = shortName
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue, unselectedColor = TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 9: VOICE DICTATION HELPER SCREEN ---
@Composable
fun VoiceKeyboardOverlay(viewModel: MainViewModel) {
    val listening by viewModel.voiceListeningState.collectAsStateWithLifecycle()
    val volume by viewModel.voiceVolumeLevel.collectAsStateWithLifecycle()
    val buffer by viewModel.keyboardInputBuffer.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF0F121F))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Voice to Text Engine", fontWeight = FontWeight.Bold, color = NeonPurple, fontSize = 13.sp)
            IconButton(onClick = {
                viewModel.stopVoiceTyping()
                viewModel.isVoiceOverlayVisible.value = false
            }) {
                Icon(Icons.Default.Close, "Close", tint = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Dictating sound ripples
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(48.dp)
        ) {
            val count = 6
            for (i in 0 until count) {
                val scale = if (listening) (volume * (1f - (i * 0.15f))).coerceAtLeast(1f) else 1f
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(8.dp * scale)
                        .background(
                            Brush.verticalGradient(listOf(ElectricBlue, NeonPurple)),
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (listening) "Speak now, listening..." else "Speech captured successfully.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (listening) {
                    viewModel.stopVoiceTyping()
                } else {
                    viewModel.startVoiceTyping()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (listening) Color.Red else ElectricBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (listening) "MUTE DIC" else "START DICTATION", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VoiceHelperScreen(viewModel: MainViewModel) {
    val listening by viewModel.voiceListeningState.collectAsStateWithLifecycle()
    val buffer by viewModel.keyboardInputBuffer.collectAsStateWithLifecycle()

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Voice Speechpad", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Acoustic Dictation Terminal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )

            Text(
                "Converts vocal waves into structured paragraphs instantly.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic microphone glow ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        if (listening) ElectricBlue.copy(alpha = 0.15f) else CosmicDarkCard,
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (listening) ElectricBlue else BorderGlass,
                        CircleShape
                    )
                    .clickable {
                        if (listening) viewModel.stopVoiceTyping() else viewModel.startVoiceTyping()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = if (listening) ElectricBlue else NeonPurple,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Output panel text box
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("SPEECH OUTPUT TERMINAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = buffer.ifEmpty { "Transcribed speech output will show up here as you talk in real time..." },
                        color = if (buffer.isEmpty()) TextMuted else TextPrimary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { viewModel.keyboardInputBuffer.value = "" }) {
                        Text("Clear Output", color = Color.Red)
                    }
                    Button(
                        onClick = { viewModel.navigateTo(MainViewModel.Screen.KeyboardSimulator) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Insert in Chat", color = Color.Black)
                    }
                }
            }
        }
    }
}

// --- SCREEN 10: PREMIUM SUBSCRIPTION SCREEN ---
@Composable
fun PremiumCenterScreen(viewModel: MainViewModel) {
    val isPremium by viewModel.isPremiumUser.collectAsStateWithLifecycle()

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Membership Panel", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(24.dp))

            // Super futuristic galaxy ring
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonPurple, ElectricBlue)),
                        CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CosmicSlateBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Premium logo",
                        tint = NeonPurple,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "KEYASSIST PRO",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonPurple,
                letterSpacing = 1.sp
            )

            Text(
                "Unleash lightning-fast context translation & unhinged generative writing power.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp)
            )

            // Features Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("EXCLUSIVE BENEFITS", fontWeight = FontWeight.Bold, color = ElectricBlue, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

                BenefitRow("Unlimited context replies matching tone perfectly")
                BenefitRow("Grammar analysis trained on large corpora")
                BenefitRow("Direct localized translation & Hinglish-to-English")
                BenefitRow("Zero message templates capacity thresholds")
                BenefitRow("Instant personalized typing metrics dashboards")
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Pricing toggle trigger
            if (isPremium) {
                Text("You are an active PRO license holder. Enjoy write speed!", color = Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.cancelPremium() },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkCard),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DEACTIVATE DEMO STATUS", color = Color.Red, fontSize = 13.sp)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicDarkCard, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Annual VIP License", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$19.99/year • Lifetime cancel option", fontSize = 11.sp, color = TextSecondary)
                    }
                    Button(
                        onClick = { viewModel.purchasePremium() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("UPGRADE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, "benefit", tint = Color.Green, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 12.sp, color = TextPrimary)
    }
}

// --- SCREEN 11: PREFERENCES SETTINGS PAGE ---
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val haptic by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val autoCaps by viewModel.autoCapitalizationEnabled.collectAsStateWithLifecycle()
    val dblSpace by viewModel.doubleSpacePeriodEnabled.collectAsStateWithLifecycle()
    val blockBadWords by viewModel.blockOffensiveWords.collectAsStateWithLifecycle()

    AmbientBlurBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HeaderBlock(title = "Settings Preference", onBack = { viewModel.navigateTo(MainViewModel.Screen.Dashboard) })

            Spacer(modifier = Modifier.height(20.dp))

            Text("KEYBOARD BEHAVIORS", fontWeight = FontWeight.Bold, color = ElectricBlue, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

            SettingsToggleRow("Haptic dynamic key feedbacks", haptic) { viewModel.hapticFeedbackEnabled.value = it }
            SettingsToggleRow("Auto capitalization starts first word", autoCaps) { viewModel.autoCapitalizationEnabled.value = it }
            SettingsToggleRow("Double tapping Space inserts period '.'", dblSpace) { viewModel.doubleSpacePeriodEnabled.value = it }
            SettingsToggleRow("Block and isolate offensive AI statements", blockBadWords) { viewModel.blockOffensiveWords.value = it }

            Spacer(modifier = Modifier.height(28.dp))

            Text("ADVANCED ACTIONS", fontWeight = FontWeight.Bold, color = NeonPurple, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

            Button(
                onClick = { viewModel.clearClipboard() },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkCard),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Text("RESET LOCAL DATABASE CACHE", color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security warning block (MANDATORY per secret management guides for prototypes!)
            GlassCard(borderColor = Color.Red.copy(alpha = 0.5f)) {
                Text("⚠️ Security Warning", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsToggleRow(text: String, state: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(CosmicDarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 13.sp, color = TextPrimary)
        Switch(
            checked = state,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = ElectricBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CosmicDarkCard
            )
        )
    }
}

// Common custom Header row block helper
@Composable
fun HeaderBlock(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(CosmicDarkCard, CircleShape).border(1.dp, BorderGlass, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ElectricBlue)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
