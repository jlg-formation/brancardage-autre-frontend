package com.example.huybrancardage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.huybrancardage.ui.theme.Blue600
import com.example.huybrancardage.ui.theme.HuyBrancardageTheme
import com.example.huybrancardage.ui.theme.White

/**
 * Écran de scan du bracelet patient
 * Affiche une interface caméra avec zone de ciblage pour QR Code/Code-barres
 */
@Composable
fun ScanBraceletScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onScanSuccess: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1F2937))
    ) {
        // Fond simulant la caméra
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF374151))
        )

        // Header transparent avec gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bouton retour
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Scanner le bracelet",
                    style = MaterialTheme.typography.titleLarge,
                    color = White
                )
            }
        }

        // Zone centrale avec le cadre de scan
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Instructions
            Text(
                text = "Placez le code-barres ou QR Code du bracelet dans le cadre.",
                style = MaterialTheme.typography.bodyMedium,
                color = White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Cadre de visée
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                // Coins du cadre
                ScanCorners()

                // Ligne de scan animée (simulation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .background(Color.Red)
                )
            }
        }

        // Actions en bas avec gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton flash
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.2f))
                        .clickable { /* Toggle flash */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flash",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Bouton de simulation de succès (pour maquette)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Blue600)
                        .clickable { onScanSuccess() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Simuler scan réussi",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanCorners() {
    val cornerSize = 32.dp
    val cornerStrokeWidth = 4.dp
    val cornerColor = Blue600

    Box(modifier = Modifier.fillMaxSize()) {
        // Coin haut-gauche
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
                .border(
                    width = cornerStrokeWidth,
                    color = cornerColor,
                    shape = RoundedCornerShape(topStart = 8.dp)
                )
                .clip(RoundedCornerShape(topStart = 8.dp))
        ) {
            Box(
                modifier = Modifier
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }

        // Coin haut-droite
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }

        // Coin bas-gauche
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }

        // Coin bas-droite
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(cornerSize)
                    .height(cornerStrokeWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(cornerStrokeWidth)
                    .height(cornerSize)
                    .background(cornerColor)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScanBraceletScreenPreview() {
    HuyBrancardageTheme {
        ScanBraceletScreen()
    }
}





