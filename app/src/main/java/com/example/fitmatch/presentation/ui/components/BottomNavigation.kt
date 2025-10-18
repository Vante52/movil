package com.example.fitmatch.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int = 0,
    val isProfile: Boolean = false
)

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit,
    profileImageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = Color.Black,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                icon = {
                    if (item.isProfile) {
                        ProfileIcon(
                            imageUrl = profileImageUrl,
                            isSelected = currentRoute == item.route
                        )
                    } else {
                        IconWithBadge(
                            icon = item.icon,
                            badgeCount = item.badgeCount,
                            isSelected = currentRoute == item.route
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun IconWithBadge(
    icon: ImageVector,
    badgeCount: Int,
    isSelected: Boolean
) {
    Box {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Color.Black else Color.Gray
        )

        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = Color(0xFF8B4513) // Color marrón como en la imagen
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            ) {
                // Espacio invisible para el badge
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileIcon(
    imageUrl: String?,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.3f))
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty() && imageUrl != "https://example.com/profile.jpg") {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                tint = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}