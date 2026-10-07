package com.example.clockstudio.presentation.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockstudio.R
import com.example.clockstudio.ads.BannerAdContainer
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

private data class LanguageOption(val tag: String, val title: Int, val flag: String)

private val supportedLanguages = listOf(
    LanguageOption("en", R.string.language_english, "🇬🇧"),
    LanguageOption("ar", R.string.language_arabic, "🇸🇦"),
    LanguageOption("tr", R.string.language_turkish, "🇹🇷"),
    LanguageOption("es", R.string.language_spanish, "🇪🇸"),
    LanguageOption("ru", R.string.language_russian, "🇷🇺"),
    LanguageOption("fr", R.string.language_french, "🇫🇷"),
    LanguageOption("pt", R.string.language_portuguese, "🇵🇹"),
    LanguageOption("id", R.string.language_indonesian, "🇮🇩"),
)

@Composable
fun LanguageScreen(
    preferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit,
) {
    val settings by preferencesRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = com.example.clockstudio.domain.model.AppSettings())
    val scope = rememberCoroutineScope()
    val selectedTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        .substringBefore(',')
        .ifBlank { settings.languageTag }
        .substringBefore('-')

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = { BannerAdContainer(Modifier.navigationBarsPadding()) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(66.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 7.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFF6A6A6A)),
                ) {
                    Text(
                        text = stringResource(R.string.select_language),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
                    )
                }
                Spacer(Modifier.width(48.dp).align(Alignment.CenterEnd))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                items(supportedLanguages, key = { it.tag }) { option ->
                    val isSelected = selectedTag.equals(option.tag, ignoreCase = true)
                    LanguageCard(
                        option = option,
                        selected = isSelected,
                        onSelect = {
                            scope.launch {
                                preferencesRepository.setLanguage(option.tag)
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(option.tag))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageCard(option: LanguageOption, selected: Boolean, onSelect: () -> Unit) {
    val description = stringResource(option.title)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .semantics { contentDescription = description }
            .clickable(role = Role.RadioButton, onClick = onSelect),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) Color(0xFF151A19) else Color(0xFF080808),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) Color(0xFF009B83) else Color(0xFF383838)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Text(
                text = stringResource(option.title),
                modifier = Modifier.weight(1f).padding(start = 7.dp),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(option.flag, fontSize = 22.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
