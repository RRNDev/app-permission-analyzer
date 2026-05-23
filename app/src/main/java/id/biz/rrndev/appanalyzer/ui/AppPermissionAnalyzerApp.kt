package id.biz.rrndev.appanalyzer.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import id.biz.rrndev.appanalyzer.BuildConfig
import id.biz.rrndev.appanalyzer.model.AnalysisSource
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.PermissionItem
import id.biz.rrndev.appanalyzer.ui.components.AppBackground
import id.biz.rrndev.appanalyzer.ui.screens.AppDetailsScreen
import id.biz.rrndev.appanalyzer.ui.screens.AppsScreen
import id.biz.rrndev.appanalyzer.ui.screens.HomeScreen
import id.biz.rrndev.appanalyzer.ui.screens.PermissionDetailsScreen
import id.biz.rrndev.appanalyzer.ui.screens.ScoreResultScreen
import id.biz.rrndev.appanalyzer.ui.screens.SettingsScreen
import id.biz.rrndev.appanalyzer.ui.theme.AppAnalyzerTheme
import id.biz.rrndev.appanalyzer.ui.viewmodel.ApkAnalyzerViewModel
import id.biz.rrndev.appanalyzer.ui.viewmodel.InstalledAppsViewModel
import id.biz.rrndev.appanalyzer.ui.viewmodel.SettingsViewModel

sealed interface AppRoute {
    data object Home : AppRoute
    data object Apps : AppRoute
    data object Settings : AppRoute
    data class Details(val analysis: AppAnalysis, val fromTab: Int = 0) : AppRoute
    data class PermissionDetails(val permission: PermissionItem, val fromTab: Int = 0) : AppRoute
    data class ScoreResult(val analysis: AppAnalysis, val fromTab: Int = 0) : AppRoute
}

private data class MainNavItem(
    val route: AppRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun AppPermissionAnalyzerApp(
    incomingApkUri: Uri? = null,
    onIncomingApkHandled: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val uriHandler = LocalUriHandler.current
    val factory = remember(application) {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val installedAppsViewModel: InstalledAppsViewModel = viewModel(factory = factory)
    val apkAnalyzerViewModel: ApkAnalyzerViewModel = viewModel(factory = factory)

    val settingsState by settingsViewModel.uiState.collectAsState()
    val installedState by installedAppsViewModel.uiState.collectAsState()
    val apkState by apkAnalyzerViewModel.uiState.collectAsState()

    LaunchedEffect(settingsState.includeSystemApps) {
        installedAppsViewModel.refresh(includeSystemApps = settingsState.includeSystemApps)
    }

    AppAnalyzerTheme(useDynamicColor = settingsState.useDynamicColor) {
        val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Home) }
        val currentRoute = backStack.last()
        var selectedAppsTab by remember { mutableIntStateOf(0) }
        var openIncomingApkDetailsWhenReady by remember { mutableStateOf(false) }
        var navForward by remember { mutableStateOf(true) }

        fun navigate(route: AppRoute) {
            navForward = true
            backStack.add(route)
        }

        fun switchMain(route: AppRoute) {
            navForward = true
            backStack.clear()
            backStack.add(route)
            if (route == AppRoute.Apps) {
                selectedAppsTab = 0
            }
        }

        fun goBack() {
            if (backStack.size > 1) {
                navForward = false
                val goingFrom = backStack.last()
                backStack.removeAt(backStack.lastIndex)
                val tabToRestore = when (goingFrom) {
                    is AppRoute.Details -> goingFrom.fromTab
                    is AppRoute.PermissionDetails -> goingFrom.fromTab
                    is AppRoute.ScoreResult -> goingFrom.fromTab
                    else -> 0
                }
                selectedAppsTab = tabToRestore
            }
        }

        LaunchedEffect(incomingApkUri) {
            incomingApkUri?.let { uri ->
                switchMain(AppRoute.Apps)
                selectedAppsTab = 1
                openIncomingApkDetailsWhenReady = true
                apkAnalyzerViewModel.analyze(
                    uri = uri,
                    deepTrackerScan = settingsState.scanTrackersDeeply
                )
                onIncomingApkHandled(uri)
            }
        }

        LaunchedEffect(
            openIncomingApkDetailsWhenReady,
            apkState.analysis?.id,
            apkState.isLoading,
            apkState.errorMessage
        ) {
            if (!openIncomingApkDetailsWhenReady || apkState.isLoading) return@LaunchedEffect

            val analysis = apkState.analysis
            if (analysis != null) {
                backStack.clear()
                backStack.add(AppRoute.Apps)
                backStack.add(AppRoute.Details(analysis, fromTab = 1))
                selectedAppsTab = 1
                openIncomingApkDetailsWhenReady = false
            } else if (apkState.errorMessage != null) {
                openIncomingApkDetailsWhenReady = false
            }
        }

        BackHandler(enabled = backStack.size > 1, onBack = ::goBack)

        AppBackground(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                topBar = {
                    AppTopBar(
                        title = currentRoute.title(context),
                        canGoBack = backStack.size > 1,
                        navForward = navForward,
                        onBack = ::goBack
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                ) {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = {
                            if (navForward) {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it / 3 } + fadeOut()
                            } else {
                                slideInHorizontally { -it / 3 } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                            }
                        },
                        label = "screen"
                    ) { route ->
                        when (route) {
                            AppRoute.Home -> HomeScreen(
                                installedState = installedState,
                                onOpenApps = { switchMain(AppRoute.Apps) }
                            )
                            AppRoute.Apps -> AppsScreen(
                                installedAppsState = installedState,
                                apkState = apkState,
                                deepTrackerScan = settingsState.scanTrackersDeeply,
                                onQueryChange = installedAppsViewModel::updateQuery,
                                onSortChange = installedAppsViewModel::updateSortMode,
                                onHighRiskOnlyChange = installedAppsViewModel::updateHighRiskOnly,
                                onRefresh = {
                                    installedAppsViewModel.forceRefresh(
                                        includeSystemApps = settingsState.includeSystemApps
                                    )
                                },
                                onOpenApp = {
                                    navigate(AppRoute.Details(it, selectedAppsTab))
                                    installedAppsViewModel.enrichApp(
                                        packageName = it.packageName,
                                        deepTrackerScan = settingsState.scanTrackersDeeply
                                    )
                                },
                                onAnalyzeApk = { uri ->
                                    selectedAppsTab = 1
                                    apkAnalyzerViewModel.analyze(
                                        uri = uri,
                                        deepTrackerScan = settingsState.scanTrackersDeeply
                                    )
                                },
                                onClear = apkAnalyzerViewModel::clearResult,
                                onOpenApkResult = { 
                                    navigate(AppRoute.Details(it, fromTab = 1)) 
                                },
                                onTabChange = { selectedAppsTab = it },
                                initialTab = selectedAppsTab
                            )
                            AppRoute.Settings -> SettingsScreen(
                                state = settingsState,
                                onIncludeSystemAppsChange = settingsViewModel::setIncludeSystemApps,
                                onDeepTrackerScanChange = settingsViewModel::setDeepTrackerScan,
                                onShowTechnicalNamesChange = settingsViewModel::setShowTechnicalPermissionNames,
                                onDynamicColorChange = settingsViewModel::setUseDynamicColor,
                                showUpdateCta = !BuildConfig.IS_PRO,
                                onOpenGooglePlay = {
                                    uriHandler.openUri(APP_PLAY_STORE_URL)
                                }
                            )
                            is AppRoute.Details -> {
                                val latestAnalysis = if (route.analysis.source == AnalysisSource.InstalledApp) {
                                    installedState.apps.firstOrNull { it.packageName == route.analysis.packageName }
                                        ?: route.analysis
                                } else {
                                    route.analysis
                                }
                                AppDetailsScreen(
                                    analysis = latestAnalysis,
                                    showTechnicalNames = settingsState.showTechnicalPermissionNames,
                                    onOpenPermission = { navigate(AppRoute.PermissionDetails(it, route.fromTab)) },
                                    onOpenScore = { navigate(AppRoute.ScoreResult(it, route.fromTab)) }
                                )
                            }
                            is AppRoute.PermissionDetails -> PermissionDetailsScreen(permission = route.permission)
                            is AppRoute.ScoreResult -> ScoreResultScreen(analysis = route.analysis)
                        }
                    }
                    AnimatedVisibility(
                        visible = currentRoute.isMainRoute(),
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        AppBottomBar(
                            currentRoute = currentRoute,
                            onSelect = ::switchMain
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    canGoBack: Boolean,
    navForward: Boolean,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            AnimatedVisibility(
                visible = canGoBack,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.action_back), color = MaterialTheme.colorScheme.primary)
                }
            }
            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    if (navForward) {
                        slideInVertically { it / 2 } + fadeIn() togetherWith
                            slideOutVertically { -it / 2 } + fadeOut()
                    } else {
                        slideInVertically { -it / 2 } + fadeIn() togetherWith
                            slideOutVertically { it / 2 } + fadeOut()
                    }
                },
                label = "top_bar_title",
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) { animatedTitle ->
                Text(
                    text = animatedTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: AppRoute,
    onSelect: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        MainNavItem(AppRoute.Home, Icons.Filled.Home, Icons.Outlined.Home),
        MainNavItem(AppRoute.Apps, Icons.Filled.Apps, Icons.Outlined.Apps),
        MainNavItem(AppRoute.Settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            )
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                items.forEach { item ->
                    val selected = currentRoute::class == item.route::class
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onSelect(item.route) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun AppRoute.title(context: Context): String {
    return when (this) {
        AppRoute.Home -> context.getString(R.string.nav_home)
        AppRoute.Apps -> context.getString(R.string.nav_apps_apk)
        AppRoute.Settings -> context.getString(R.string.nav_settings)
        is AppRoute.Details -> appNameSafe(analysis)
        is AppRoute.PermissionDetails -> permission.displayName
        is AppRoute.ScoreResult -> context.getString(R.string.title_privacy_score)
    }
}

private fun appNameSafe(analysis: AppAnalysis): String {
    return analysis.appName.ifBlank { analysis.packageName }
}

private fun AppRoute.isMainRoute(): Boolean {
    return this == AppRoute.Home ||
        this == AppRoute.Apps ||
        this == AppRoute.Settings
}

private const val APP_PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=id.biz.rrndev.appanalyzer.pro"
