package com.palmah.cafe.amirtham.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.ai_studio_placeholder_description
import amirtham.shared.generated.resources.ai_studio_tab_label
import amirtham.shared.generated.resources.app_name
import amirtham.shared.generated.resources.digital_signage
import amirtham.shared.generated.resources.menu_tab_label
import amirtham.shared.generated.resources.open_menu
import amirtham.shared.generated.resources.outlet_prefix
import amirtham.shared.generated.resources.powered_by
import amirtham.shared.generated.resources.profile
import amirtham.shared.generated.resources.sign_out
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.palmah.cafe.amirtham.digitalSignage.ui.DigitalSignageDisplay
import com.palmah.cafe.amirtham.digitalSignage.ui.DigitalSignageListScreen
import com.palmah.cafe.amirtham.home.viewmodel.SideMenuViewModel
import com.palmah.cafe.amirtham.menu.list.ui.MenuListScreen
import com.palmah.cafe.amirtham.ui.theme.Terracotta
import com.palmah.cafe.amirtham.ui.theme.TerracottaContainer
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private enum class HomeDestination(val label: StringResource) {
    Menu(Res.string.menu_tab_label),
    AiStudio(Res.string.ai_studio_tab_label),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: FirebaseUser,
    onSignOut: () -> Unit,
    sideMenuViewModel: SideMenuViewModel = koinViewModel<SideMenuViewModel>(),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var selectedDestination by remember { mutableStateOf(HomeDestination.Menu) }
    var showDigitalSignageDisplay by remember { mutableStateOf(false) }
    val sideMenuUiState by sideMenuViewModel.uiState.collectAsState()
    val userInfo = sideMenuUiState.userInfo

    if (showDigitalSignageDisplay) {
        DigitalSignageDisplay(onClose = { showDigitalSignageDisplay = false })
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(TerracottaContainer)
                            .border(2.dp, Terracotta, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(56.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val brand = userInfo?.brand.orEmpty()
                    if (brand.isNotBlank()) {
                        Text(
                            text = brand.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    val outlet = userInfo?.outlet.orEmpty()
                    if (outlet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedOutlet = if(outlet.contains(brand)) {
                            outlet.split(" ").let {
                                if(it.size > 2){
                                    it[1]
                                } else {
                                    outlet
                                }
                            }
                        } else {
                            outlet
                        }
                        Text(
                            text = stringResource(Res.string.outlet_prefix, formattedOutlet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = userInfo?.email ?: user.email ?: user.uid,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val displayName = userInfo?.displayName ?: user.displayName
                    if (!displayName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(Res.string.profile)) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    selected = false,
                    onClick = { coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(Res.string.digital_signage)) },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showDigitalSignageDisplay = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(Res.string.sign_out)) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onSignOut()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.powered_by, stringResource(Res.string.app_name)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(Res.string.open_menu))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = selectedDestination == HomeDestination.Menu,
                        onClick = { selectedDestination = HomeDestination.Menu },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text(stringResource(HomeDestination.Menu.label)) },
                        colors = homeNavigationItemColors(),
                    )
                    NavigationBarItem(
                        selected = selectedDestination == HomeDestination.AiStudio,
                        onClick = { selectedDestination = HomeDestination.AiStudio },
                        icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                        label = { Text(stringResource(HomeDestination.AiStudio.label)) },
                        colors = homeNavigationItemColors(),
                    )
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (selectedDestination) {
                    HomeDestination.Menu -> MenuListScreen()
                    HomeDestination.AiStudio -> DigitalSignageListScreen()
                }
            }
        }
    }
}

@Composable
private fun homeNavigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = Terracotta,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

// TODO: replace with the real "AI Studio" destination.
@Composable
private fun AiStudioTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.ai_studio_tab_label),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(Res.string.ai_studio_placeholder_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
