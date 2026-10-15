package com.humboshot.koillection.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.humboshot.koillection.R
import com.humboshot.koillection.theme.KoillectionTheme

/** One entry of the side menu. A null [label] draws a divider, like the web sidebar's separators. */
data class DrawerEntry(
    val label: String?,
    val icon: (@Composable () -> Unit)? = null,
    val selected: Boolean = false,
    val onClick: () -> Unit = {},
)

/** The side menu. It follows the web sidebar: logo, sections, then the user at the bottom. The user opens the profile. */
@Composable
fun AppDrawer(
    entries: List<DrawerEntry>,
    username: String?,
    serverHost: String?,
    onProfileClick: () -> Unit,
) {
    val colors = KoillectionTheme.colors
    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colors.sidebarSelected,
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = colors.onSidebar,
        unselectedIconColor = colors.onSidebar,
        selectedTextColor = colors.onSidebar,
        unselectedTextColor = colors.onSidebar,
    )

    ModalDrawerSheet(
        drawerContainerColor = colors.sidebar,
        drawerContentColor = colors.onSidebar,
        drawerShape = RectangleShape,
        // The logo color must reach under the status bar, so this drawer handles the insets itself.
        windowInsets = WindowInsets(0),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.sidebarLogo)
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(Modifier.verticalScroll(rememberScrollState())) {
                entries.forEach { entry ->
                    if (entry.label == null) {
                        HorizontalDivider(
                            color = colors.sidebarDivider,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    } else {
                        NavigationDrawerItem(
                            label = { Text(entry.label) },
                            icon = entry.icon,
                            selected = entry.selected,
                            onClick = entry.onClick,
                            shape = RectangleShape,
                            colors = itemColors,
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = colors.sidebarDivider, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClick)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Column {
                Text(username.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                serverHost?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSidebar.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
