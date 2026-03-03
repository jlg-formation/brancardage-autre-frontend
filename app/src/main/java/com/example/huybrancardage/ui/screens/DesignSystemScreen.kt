package com.example.huybrancardage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.ActionMenuCard
import com.example.huybrancardage.ui.theme.BrancardageTextField
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.PatientCard
import com.example.huybrancardage.ui.theme.PrimaryButton
import com.example.huybrancardage.ui.theme.SecondaryButton
import com.example.huybrancardage.ui.theme.TertiaryButton

/**
 * Design System showcase screen
 * Displays all reusable components for the application
 */
@Composable
fun DesignSystemScreen(modifier: Modifier = Modifier) {
    var textValue by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
            .verticalScroll(rememberScrollState())
    ) {
        BrancardageTopAppBar(
            title = "Design System",
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Buttons Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Boutons",
                    style = MaterialTheme.typography.headlineMedium
                )

                PrimaryButton(
                    text = "Valider",
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )

                SecondaryButton(
                    text = "Prendre une photo",
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )

                TertiaryButton(
                    text = "Annuler",
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Text Fields Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Champs de saisie",
                    style = MaterialTheme.typography.headlineMedium
                )

                BrancardageTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = "Nom du patient",
                    placeholder = "Ex: Dupont",
                    modifier = Modifier.fillMaxWidth()
                )

                BrancardageTextField(
                    value = "",
                    onValueChange = { },
                    label = "Rechercher un service",
                    placeholder = "Cardiologie...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Patient Card Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Carte d'information Patient",
                    style = MaterialTheme.typography.headlineMedium
                )

                PatientCard(
                    name = "Jean Dupont",
                    initials = "JD",
                    ipp = "123456789",
                    dateOfBirth = "12/05/1980",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Action Menu Card Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cartes d'action (Menu)",
                    style = MaterialTheme.typography.headlineMedium
                )

                ActionMenuCard(
                    title = "Recherche Manuelle",
                    description = "Saisir nom, IPP...",
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )

                ActionMenuCard(
                    title = "Scan Bracelet",
                    description = "Scanner le code-barres du bracelet",
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DesignSystemScreenPreview() {
    HuyBrancardageTheme {
        DesignSystemScreen()
    }
}

