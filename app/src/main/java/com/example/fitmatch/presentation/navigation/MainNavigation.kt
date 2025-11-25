package com.example.fitmatch.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitmatch.presentation.ui.components.BottomNavItem
import com.example.fitmatch.presentation.ui.components.BottomNavigationBar
import com.example.fitmatch.presentation.ui.screens.auth.ui.CompleteProfileScreen
import com.example.fitmatch.presentation.ui.screens.auth.ui.LoginScreen
import com.example.fitmatch.presentation.ui.screens.auth.ui.RegisterScreen
import com.example.fitmatch.presentation.ui.screens.auth.ui.WelcomeScreen
import com.example.fitmatch.presentation.viewmodel.login.LoginViewModel
import com.example.fitmatch.presentation.viewmodel.login.RegisterViewModel
import com.example.fitmatch.presentation.ui.screens.cliente.CartScreen
import com.example.fitmatch.presentation.ui.screens.cliente.ClienteDashboardScreen
import com.example.fitmatch.presentation.ui.screens.cliente.FavoritesScreen
import com.example.fitmatch.presentation.ui.screens.vendedor.VendedorDashboardScreen
import com.example.fitmatch.presentation.ui.screens.cliente.ui.PreferencesFlowScreen
import com.example.fitmatch.presentation.ui.screens.cliente.ui.ProfileScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.ChatListScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.ChatScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.DeliveryPickupScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.NotificationsScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.OrdersScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.ProductDetailScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.SearchScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.StoreProfileScreen
import com.example.fitmatch.presentation.ui.screens.common.ui.TitoChatScreen
import com.example.fitmatch.presentation.ui.screens.vendedor.CreateProductScreen

// Si tienes pantallas de vendedor, impórtalas y úsalas en el if(role=="Vendedor")

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRole = navBackStackEntry?.arguments?.getString("role")

    // Rutas donde se muestra la bottom bar
    val bottomBarRoutes = remember {
        setOf(
            AppScreens.Cart.route,
            AppScreens.Notifications.route,
            AppScreens.Profile.route,
            AppScreens.DeliveryPickup.route,
            AppScreens.Favorites.route,
            AppScreens.ChatList.route
        )
    }

    // Home que aparece en la bottom bar (según rol actual si existe; por defecto Cliente)
    val homeRouteForBar = if (currentRole.equals("Vendedor", ignoreCase = true)) {
        AppScreens.Home.withRole("Vendedor")
    } else {
        AppScreens.Home.withRole("Cliente")
    }

    val bottomNavItems = remember(homeRouteForBar) {
        listOf(
            BottomNavItem(
                route = homeRouteForBar,
                icon = Icons.Filled.Home,
                label = "Home"
            ),
            BottomNavItem(
                route = AppScreens.Cart.route,
                icon = Icons.Filled.ShoppingCart,
                label = "Cart"
            ),
            BottomNavItem(
                route = AppScreens.ChatList.route,
                icon = Icons.Filled.Email,
                label = "Chat_list"
            ),
            BottomNavItem(
                route = AppScreens.Notifications.route,
                icon = Icons.Filled.Notifications,
                label = "Notifications",
                badgeCount = 5
            ),
            BottomNavItem(
                route = AppScreens.Profile.route,
                icon = Icons.Filled.Person,
                label = "Profile",
                isProfile = true
            )
        )
    }

    Scaffold(
        bottomBar = {
            if (
                currentRoute in bottomBarRoutes ||
                currentRoute?.startsWith("home/") == true // cubre home/{role}
            ) {
                BottomNavigationBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute ?: homeRouteForBar,
                    onItemClick = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    profileImageUrl = null
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.Welcome.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Onboarding
            composable(AppScreens.Welcome.route) {
                WelcomeScreen(
                    onCreateAccount = { navController.navigate(AppScreens.Register.route) },
                    onContinueWithEmail = { navController.navigate(AppScreens.Login.route) },
                    onContinueWithGoogle = { navController.navigate(AppScreens.Login.route) },
                    onContinueWithFacebook = { navController.navigate(AppScreens.Login.route) },
                    // NUEVOS CALLBACKS
                    onNavigateToCompleteProfile = { userId ->
                        navController.navigate(AppScreens.CompleteProfile.withUserId(userId)) {
                            popUpTo(AppScreens.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToHome = { userId ->
                        // Usuario existente con perfil completo
                        navController.navigate(AppScreens.Home.withRole("Cliente")) {
                            popUpTo(AppScreens.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppScreens.Login.route) {
                // ViewModel con scope a este destino de navegación
                val loginViewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = loginViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLoginSuccess = {
                        // Navegar a Home (por defecto Cliente)
                        navController.navigate(AppScreens.Home.withRole("Cliente")) {
                            popUpTo(AppScreens.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onForgotPasswordClick = {
                        // TODO: Implementar recuperación de contraseña
                    }
                )
            }

            composable(AppScreens.Register.route) {
                // ViewModel con scope a este destino
                val registerViewModel: RegisterViewModel = viewModel()

                RegisterScreen(
                    viewModel = registerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onRegisterSuccess = { role ->
                        // CAMBIO: Solo clientes van a Preferences, vendedores van directo a su home
                        if (role.equals("Vendedor", ignoreCase = true)) {
                            navController.navigate(AppScreens.Home.withRole(role)) {
                                popUpTo(AppScreens.Welcome.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Clientes van a Preferences
                            navController.navigate(AppScreens.Preferences.route) {
                                popUpTo(AppScreens.Welcome.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(
                route = AppScreens.CompleteProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""

                CompleteProfileScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onContinue = { role ->
                        // Después de completar el perfil, navegar según el rol
                        if (role.equals("Vendedor", ignoreCase = true)) {
                            navController.navigate(AppScreens.Home.withRole(role)) {
                                popUpTo(AppScreens.Welcome.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Clientes van a Preferences
                            navController.navigate(AppScreens.Preferences.route) {
                                popUpTo(AppScreens.Welcome.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(AppScreens.Preferences.route) {
                PreferencesFlowScreen (
                    onBackToRegister = { navController.popBackStack() },
                    onFinishClick = { selections ->
                        // Aquí ya tienes un Map<PreferenceType, Set<String>>
                        // con lo que eligió el usuario en cada categoría
                        // TODO: Guardar en ViewModel / backend
                        navController.navigate(AppScreens.Home.withRole("Cliente")) {
                            popUpTo(AppScreens.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }


            // Home con rol
            composable(
                route = AppScreens.Home.route,
                arguments = listOf(navArgument("role") { type = NavType.StringType })
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "Cliente"

                if (role.equals("Vendedor", ignoreCase = true)) {
                    VendedorDashboardScreen(
                        onEnviosClick = {},
                        onComentariosClick = {},
                        onEstadisticasClick = {},
                        onMisPedidosClick = {},
                        onAgregarProductoClick = {navController.navigate(AppScreens.Create.route)},
                        onMostrarProductosClick = {}
                    )
                } else {
                    ClienteDashboardScreen(
                        onBackClick = { navController.popBackStack() },
                        //onFollowClick = { },
                        //onProductClick = { navController.navigate(AppScreens.ProductDetail.route) },
                        //onStoreClick = { navController.navigate(AppScreens.StoreProfile.route) },
                        onFilterClick = { navController.navigate(AppScreens.Search.route) },
                        //onOpenComments = { /* ... */ },
                        //onAddToCart = { /* ... */ }
                    )
                }
            }

            composable(AppScreens.Search.route) {
                var isTemperatureFilterEnabled by remember { mutableStateOf(false) }

                SearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = {
                        navController.navigate(AppScreens.ProductDetail.route)
                    },
                    isTemperatureFilterEnabled = isTemperatureFilterEnabled,
                    onToggleTemperatureFilter = { enabled ->
                        isTemperatureFilterEnabled = enabled
                    }
                )
            }


            composable(AppScreens.ProductDetail.route) {
                ProductDetailScreen(
                    onBuyClick = { navController.navigate(AppScreens.Cart.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppScreens.Cart.route) {
                CartScreen(
                    onCheckoutClick = { navController.navigate(AppScreens.DeliveryPickup.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppScreens.Orders.route) {
                OrdersScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppScreens.DeliveryPickup.route) {
                DeliveryPickupScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }


            // Chat genérico (si tienes una pantalla sin id)
            composable(AppScreens.Chat.route) {
                ChatScreen(
                    onBackClick = { navController.popBackStack() },
                    onMoreClick = {},
                    onCallClick = {}
                )
            }

            // Chat list -> abre chat por id o Tito
            composable(AppScreens.ChatList.route) {
                ChatListScreen(
                    onOpenChat = { chatId, isTito ->
                        if (isTito) {
                            navController.navigate(AppScreens.TitoChat.route)
                        } else {
                            navController.navigate("chat/$chatId")
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                    onNewChat = { /* ... */ }
                )
            }

            composable(AppScreens.TitoChat.route) {
                TitoChatScreen(
                    onBackClick = { navController.popBackStack() },
                    onViewProduct = { /* navController.navigate(AppScreens.ProductDetail.route) */ },
                    onViewProfile = { /* navController.navigate(AppScreens.StoreProfile.route) */ }
                )
            }

            composable(AppScreens.Favorites.route) {
                FavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onCartClick = {},
                    onAddCategory = {},
                    onOpenProduct = {}
                )
            }

            composable(AppScreens.Notifications.route) {
                NotificationsScreen(
                    onNotificationClick = {},
                    onMarkAllRead = {}
                )
            }

            composable(AppScreens.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onSavedClick = { navController.navigate(AppScreens.Favorites.route) },
                    onLogoutClick = {
                        navController.navigate(AppScreens.Welcome.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppScreens.StoreProfile.route) {
                StoreProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onFollowClick = {},
                    onProductClick = {}
                )
            }
            composable (AppScreens.Create.route){
                CreateProductScreen (
                    onCloseClick = {navController.popBackStack()}
                )
            }

            //Completar el perfil despues de continuar con Google o Facebook
            /*
            composable("completeProfile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                CompleteProfileScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onContinue = {
                        // TODO: Navegar a Preferences o Home según el rol
                        navController.navigate(AppScreens.Home.withRole("Cliente")) {
                            popUpTo(AppScreens.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }
            */
        }
    }
}