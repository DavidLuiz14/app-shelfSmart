package com.example.appshelfsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appshelfsmart.data.Recipe
import com.example.appshelfsmart.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {
    
    private val repository = RecipeRepository()
    
    private val _completeRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val completeRecipes: StateFlow<List<Recipe>> = _completeRecipes
    
    private val _almostCompleteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val almostCompleteRecipes: StateFlow<List<Recipe>> = _almostCompleteRecipes
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    fun searchRecipes(availableIngredients: List<String>) {
        if (availableIngredients.isEmpty()) {
            _errorMessage.value = "No hay ingredientes en tu inventario"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.searchRecipes(availableIngredients)
            
            if (result.isSuccess) {
                val recipeMap = result.getOrNull() ?: emptyMap()
                _completeRecipes.value = repository.getCompleteRecipes(recipeMap)
                _almostCompleteRecipes.value = repository.getAlmostCompleteRecipes(recipeMap)
                
                if (_completeRecipes.value.isEmpty() && _almostCompleteRecipes.value.isEmpty()) {
                    _errorMessage.value = "No se encontraron recetas con tus ingredientes"
                }
            } else {
                _errorMessage.value = "Error al buscar recetas: ${result.exceptionOrNull()?.message}"
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
