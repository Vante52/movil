package com.example.fitmatch.navigation

sealed class AppScreens (val route: String) {
    object Welcome : AppScreens("welcome")
    object Register : AppScreens("register")
    object Login : AppScreens ("login")
    object Preferences : AppScreens("preferences")
    object CompleteProfile : AppScreens("complete_profile/{userId}") {
        fun withUserId(userId: String) = "complete_profile/$userId"
    }
    object Home : AppScreens("home/{role}") {
        fun withRole(role: String) = "home/$role" // role: "Cliente" | "Vendedor"
    }
    object Search : AppScreens("search")
    object ProductDetail : AppScreens("product_detail")
    object Cart : AppScreens("cart")
    object Orders : AppScreens("orders")
    object DeliveryPickup : AppScreens("delivery_pickup")
    object Chat : AppScreens("chat/{chatId}")
    object Notifications : AppScreens("notifications")
    object Profile : AppScreens("profile")
    object StoreProfile : AppScreens("store_profile")
    object ChatList : AppScreens("chat_list")
    object TitoChat : AppScreens("tito_chat")
    object Favorites : AppScreens("favorites")
    object Create : AppScreens("create_product")
}