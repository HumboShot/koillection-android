package com.humboshot.koillection.ui.navigation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.humboshot.koillection.R
import com.humboshot.koillection.theme.KoillectionTheme

/** The side menu. It follows the web sidebar: logo, sections, then the user and Log out at the bottom. */
@Composable
fun AppDrawer(
    collectionsSelected: Boolean,
    username: String?,
    serverHost: String?,
    onCollectionsClick: () -> Unit,
    onSignOut: () -> Unit,
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
                text = "Koillection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.sidebarLogo)
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            NavigationDrawerItem(
                label = { Text("Collections") },
                icon = { Icon(painterResource(R.drawable.ic_collections), contentDescription = null) },
                selected = collectionsSelected,
                onClick = onCollectionsClick,
                shape = RectangleShape,
                colors = itemColors,
            )
        }

        HorizontalDivider(color = colors.sidebarDivider, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
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
        NavigationDrawerItem(
            label = { Text("Log out") },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            selected = false,
            onClick = onSignOut,
            shape = RectangleShape,
            colors = itemColors,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        )
    }
}
