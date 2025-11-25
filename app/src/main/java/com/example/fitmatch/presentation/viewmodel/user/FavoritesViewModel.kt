package com.example.fitmatch.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.presentation.ui.screens.cliente.state.FavoriteProductState
import com.example.fitmatch.presentation.ui.screens.cliente.state.FavoritesUiState
import com.example.fitmatch.presentation.ui.screens.cliente.state.FilterCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class FavoritesViewModel : ViewModel() {

    // ========== ESTADO ==========
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }


 //cambia la carpeta / filtro de la categoria
    fun onFilterSelected(filterName: String) {
        _uiState.update { it.copy(selectedFilter = filterName) }
    }

   //agrega una nueva "carpeta" de favoritos para filtro
    fun onAddCategory(categoryName: String) {
        viewModelScope.launch {
            // TODO: Validar categoría única y llamar al repositorio
            val newCategory = FilterCategory(name = categoryName, count = 0)

            _uiState.update { currentState ->
                currentState.copy(
                    filters = currentState.filters + newCategory
                )
            }
        }
    }

    //agrega producto al carrito
    fun onAddToCart(productId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                products = currentState.products.map { product ->
                    if (product.id == productId) {
                        product.copy(inCartCount = product.inCartCount + 1)
                    } else product
                }
            )
        }
    }

    //elimina producto de fav
    fun onRemoveFromFavorites(productId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedProducts = currentState.products.filter { it.id != productId }
                currentState.copy(
                    products = updatedProducts,
                    filters = recalculateFilters(updatedProducts)
                )
            }
        }
    }

    //detalle del producto
    fun onOpenProduct(productId: String, onNavigate: (String) -> Unit) {
        onNavigate(productId)
    }


    //carga los productos desde la bdd a trvés del repository
    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: Llamada real al repositorio
            delay(500)

            val mockProducts = listOf(
                FavoriteProductState("1", "Oficina", "Blazer Sastre", price = "$189.900"),
                FavoriteProductState("2", "Streetwear", "Sneakers Eco", price = "$239.900", inCartCount = 2),
                FavoriteProductState("3", "Jeans", "Jeans Classic", price = "$129.900"),
                FavoriteProductState("4", "Zapatos", "Sandalias Midi", price = "$159.900"),
                FavoriteProductState("5", "Eventos", "Vestido Gala", price = "$329.900"),
                FavoriteProductState("6", "Streetwear", "Bolso Mini", price = "$159.900")
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    products = mockProducts,
                    filters = recalculateFilters(mockProducts)
                )
            }
        }
    }

    //recalcula filtros / carpetas según lo que uno guarde
    private fun recalculateFilters(products: List<FavoriteProductState>): List<FilterCategory> {
        val categoryCounts = products.groupingBy { it.category }.eachCount()

        val filters = mutableListOf(
            FilterCategory("Todas", products.size)
        )

        categoryCounts.forEach { (category, count) ->
            filters.add(FilterCategory(category, count))
        }

        return filters
    }
}