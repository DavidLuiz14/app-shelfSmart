package com.example.appshelfsmart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    alertsCount: Int = 0
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.appshelfsmart.R.drawable.despensa_llena),
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
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "ShelfSmart",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ) 
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                contentDescription = "Configuraciones"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title removed from here

                MenuButton(text = "🛒 Llegué del súper", onClick = onNavigateToRegistration)
                MenuButton(text = "🥫 Inventario", onClick = onNavigateToInventory)
                
                // Alerts button with badge
                Button(
                    onClick = onNavigateToAlerts,
                    modifier = Modifier
                        .fillMaxWidth(0.60f)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    BadgedBox(
                        badge = {
                            if (alertsCount > 0) {
                                Badge {
                                    Text(text = alertsCount.toString())
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "🔔 Alertas", 
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
                
                MenuButton(text = "🍳 ¿Qué puedo cocinar?", onClick = onNavigateToRecipes)
                // Settings button removed
            }
        }
    }
}


@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.60f)
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
