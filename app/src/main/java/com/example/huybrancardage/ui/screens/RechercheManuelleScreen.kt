package com.example.huybrancardage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.BrancardageTextField
import com.example.huybrancardage.ui.theme.BrancardageTopAppBar
import com.example.huybrancardage.ui.theme.Gray50
import com.example.huybrancardage.ui.theme.Gray500
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.PrimaryButton

/**
 * Écran de recherche manuelle de patient
 * Formulaire avec nom, prénom, IPP et numéro de sécurité sociale
 */
@Composable
fun RechercheManuelleScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var ipp by remember { mutableStateOf("") }
    var numeroSecu by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        BrancardageTopAppBar(
            title = "Recherche patient",
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Instructions
            Text(
                text = "Saisissez au moins un critère pour identifier le patient.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Formulaire de recherche
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Champ Nom
                BrancardageTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = "Nom",
                    placeholder = "Ex: Dupont",
                    modifier = Modifier.fillMaxWidth()
                )

                // Champ Prénom
                BrancardageTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = "Prénom",
                    placeholder = "Ex: Jean",
                    modifier = Modifier.fillMaxWidth()
                )

                // Séparateur "OU"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OU",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray500
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                // Champ IPP
                BrancardageTextField(
                    value = ipp,
                    onValueChange = { ipp = it },
                    label = "Numéro de patient (IPP)",
                    placeholder = "Ex: 123456789",
                    modifier = Modifier.fillMaxWidth()
                )

                // Champ Numéro de sécu
                BrancardageTextField(
                    value = numeroSecu,
                    onValueChange = { numeroSecu = it },
                    label = "Numéro de sécurité sociale",
                    placeholder = "Ex: 1 80 05 75...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Bouton de recherche
            PrimaryButton(
                text = "Rechercher",
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RechercheManuelleScreenPreview() {
    HuyBrancardageTheme {
        RechercheManuelleScreen()
    }
}


