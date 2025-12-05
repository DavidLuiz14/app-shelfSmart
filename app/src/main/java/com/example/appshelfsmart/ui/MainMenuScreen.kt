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

@Composable
fun MainMenuScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    alertsCount: Int = 0
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AppShelfSmart",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            MenuButton(text = "Llegué del súper (Registro)", onClick = onNavigateToRegistration)
            MenuButton(text = "Inventario", onClick = onNavigateToInventory)
            
            // Alerts button with badge
            Button(
                onClick = onNavigateToAlerts,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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
                    Text(text = "🔔 Alertas", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            MenuButton(text = "Recetas", onClick = onNavigateToRecipes)
            MenuButton(text = "Configuraciones", onClick = onNavigateToSettings)
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
