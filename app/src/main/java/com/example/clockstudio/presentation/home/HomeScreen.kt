package com.example.clockstudio.presentation.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import com.example.clockstudio.R
import com.example.clockstudio.ads.BannerAdContainer
import com.example.clockstudio.clock.digital.DigitalClockCatalog
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.presentation.components.ClockLogo
import com.example.clockstudio.presentation.components.rememberBatteryInfo
import com.example.clockstudio.presentation.components.rememberCurrentTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

private val homeCategories = ClockCategory.entries

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenWallpaper: (WallpaperItem) -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = uiState.selectedCategory.ordinal) { homeCategories.size }

    LaunchedEffect(uiState.selectedCategory) {
        if (pagerState.currentPage != uiState.selectedCategory.ordinal) {
            pagerState.animateScrollToPage(uiState.selectedCategory.ordinal)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page -> homeCategories.getOrNull(page)?.let(viewModel::selectCategory) }
    }

    val secondsInPreview = when (uiState.selectedCategory) {
        ClockCategory.CUSTOM -> false
        ClockCategory.DIGITAL -> uiState.wallpapersFor(ClockCategory.DIGITAL)
            .any { DigitalClockCatalog.find(it.clockStyleId).showSeconds }
        ClockCategory.ANALOG -> true
        ClockCategory.SMART -> uiState.appSettings.showSeconds
    }
    val now = rememberCurrentTime(updateEverySecond = secondsInPreview)
    val battery = rememberBatteryInfo(active = uiState.selectedCategory == ClockCategory.SMART)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight().widthIn(max = 328.dp),
                drawerContainerColor = Color(0xFF202020),
                drawerContentColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            ) {
                DrawerContent(
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = { category ->
                        viewModel.selectCategory(category)
                        scope.launch { drawerState.close() }
                    },
                    onPrivacy = { scope.launch { drawerState.close() }; onOpenPrivacy() },
                    onSettings = { scope.launch { drawerState.close() }; onOpenSettings() },
                    favoritesActive = uiState.showFavoritesOnly,
                    onFavorites = {
                        viewModel.setFavoritesOnly(!uiState.showFavoritesOnly)
                        scope.launch { drawerState.close() }
                    },
                    onShare = {
                        scope.launch { drawerState.close() }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message))
                        }
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app)))
                    },
                    onRate = {
                        scope.launch { drawerState.close() }
                        val packageUri = Uri.parse("market://details?id=${context.packageName}")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, packageUri))
                        } catch (_: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                        }
                    },
                )
            }
        },
    ) {
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
                HomeTopBar(
                    onMenu = { scope.launch { drawerState.open() } },
                    onLanguage = onOpenLanguage,
                )
                CategorySelector(
                    selected = uiState.selectedCategory,
                    hapticEnabled = uiState.appSettings.hapticFeedback,
                    onSelect = { category ->
                        viewModel.selectCategory(category)
                        scope.launch { pagerState.animateScrollToPage(category.ordinal) }
                    },
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    key = { page -> homeCategories[page].key },
                ) { page ->
                    val category = homeCategories[page]
                    WallpaperGrid(
                        category = category,
                        wallpapers = if (uiState.showFavoritesOnly) {
                            uiState.wallpapersFor(category).filter { it.id in uiState.favoriteIds }
                        } else {
                            uiState.wallpapersFor(category)
                        },
                        time = now,
                        batteryPercent = battery?.percent,
                        onWallpaperClick = { item ->
                            viewModel.openWallpaper(item.id)
                            onOpenWallpaper(item)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(onMenu: () -> Unit, onLanguage: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(Color.Black),
    ) {
        Text(
            text = stringResource(R.string.home_title),
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 104.dp),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.open_menu), tint = Color.White)
            }
            IconButton(onClick = onLanguage) {
                Icon(Icons.Filled.Language, contentDescription = stringResource(R.string.change_language), tint = Color.White)
            }
        }
    }
}

@Composable
private fun CategorySelector(selected: ClockCategory, hapticEnabled: Boolean, onSelect: (ClockCategory) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        homeCategories.forEach { category ->
            val isSelected = selected == category
            val background by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF009B83) else Color.Transparent,
                label = "category-pill-background",
            )
            val foreground by animateColorAsState(
                targetValue = Color.White,
                label = "category-pill-foreground",
            )
            Surface(
                onClick = {
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelect(category)
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(50),
                color = background,
                contentColor = foreground,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(category.labelRes()),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    selectedCategory: ClockCategory,
    onSelectCategory: (ClockCategory) -> Unit,
    onPrivacy: () -> Unit,
    onSettings: () -> Unit,
    favoritesActive: Boolean,
    onFavorites: () -> Unit,
    onShare: () -> Unit,
    onRate: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF202020)).padding(horizontal = 12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 38.dp, bottom = 24.dp, start = 13.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            ClockLogo(logoSize = 82.dp, contentDescription = stringResource(R.string.drawer_header))
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.drawer_header), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        DrawerItem(stringResource(R.string.smart_wallpaper), Icons.Filled.SmartToy, selectedCategory == ClockCategory.SMART) { onSelectCategory(ClockCategory.SMART) }
        DrawerItem(stringResource(R.string.analog_wallpaper), Icons.Filled.AccessTime, selectedCategory == ClockCategory.ANALOG) { onSelectCategory(ClockCategory.ANALOG) }
        DrawerItem(stringResource(R.string.digital_wallpaper), Icons.Filled.Numbers, selectedCategory == ClockCategory.DIGITAL) { onSelectCategory(ClockCategory.DIGITAL) }
        DrawerItem(stringResource(R.string.privacy_policy), Icons.Filled.PrivacyTip, false, onClick = onPrivacy)
        DrawerItem(stringResource(R.string.share_app), Icons.Filled.Share, false, onClick = onShare)
        DrawerItem(stringResource(R.string.rate_app), Icons.Filled.Star, false, onClick = onRate)
        DrawerItem(stringResource(R.string.settings_title), Icons.Filled.Settings, false, onClick = onSettings)
        DrawerItem(stringResource(R.string.favorites), Icons.Filled.Favorite, favoritesActive, onClick = onFavorites)
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF006C5D),
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = Color(0xFFBDBDBD),
            unselectedTextColor = Color(0xFFECECEC),
        ),
    )
}

private fun ClockCategory.labelRes(): Int = when (this) {
    ClockCategory.CUSTOM -> R.string.category_custom
    ClockCategory.DIGITAL -> R.string.category_digital
    ClockCategory.ANALOG -> R.string.category_analog
    ClockCategory.SMART -> R.string.category_smart
}
