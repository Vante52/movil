package com.example.fitmatch.presentation.ui.screens.cliente.state

//estado de la pantalla
data class FavoritesUiState(
    val products: List<FavoriteProductState> = emptyList(),
    val filters: List<FilterCategory> = emptyList(),
    val selectedFilter: String = "Todas",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
   //filtro por la categoria
    val filteredProducts: List<FavoriteProductState>
        get() = if (selectedFilter == "Todas") {
            products
        } else {
            products.filter { it.category == selectedFilter }
        }

    val isEmpty: Boolean
        get() = products.isEmpty()
}

//producto
data class FavoriteProductState(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String? = null,
    val price: String,
    val inCartCount: Int = 0,
    val imageUrl: String? = null
)

//filtro
data class FilterCategory(
    val name: String,
    val count: Int
)