package com.example.appshelfsmart.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appshelfsmart.data.Recipe

import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.appshelfsmart.R.drawable.cocina),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
        )

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(recipe.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Recipe Image
                AsyncImage(
                    model = recipe.thumbnail,
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
                
                    Column(modifier = Modifier.padding(16.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Category and Area
                                Text(
                                    text = "${recipe.category} • ${recipe.area}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ingredients Section
                                Text(
                                    text = "Ingredientes",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                recipe.ingredients.forEach { ingredient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (ingredient.available)
                                                Icons.Default.CheckCircle
                                            else
                                                Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (ingredient.available)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "${ingredient.measure} ${ingredient.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (ingredient.available)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Instructions Section
                                Text(
                                    text = "Instrucciones",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = recipe.instructions,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                // YouTube Link (if available)
                                if (!recipe.youtubeUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Button(
                                        onClick = { /* TODO: Open YouTube link */ },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver video en YouTube")
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}
