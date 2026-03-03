package com.example.huybrancardage.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Primary button component for main actions
 *
 * @param text Button text
 * @param onClick Callback when button is clicked
 * @param modifier Optional modifier
 * @param enabled Whether the button is enabled
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) Blue600 else Gray300,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Secondary button component for alternative actions
 *
 * @param text Button text
 * @param onClick Callback when button is clicked
 * @param modifier Optional modifier
 * @param enabled Whether the button is enabled
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = if (enabled) Blue600 else Gray300,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (enabled) White else Gray100,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Blue600 else Gray300,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Tertiary button component for cancellation/return actions
 *
 * @param text Button text
 * @param onClick Callback when button is clicked
 * @param modifier Optional modifier
 * @param enabled Whether the button is enabled
 */
@Composable
fun TertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) Gray100 else Gray200,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Gray700 else Gray500,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Reusable card component with rounded corners and shadow
 *
 * @param modifier Optional modifier
 * @param content Card content
 */
@Composable
fun BrancardageCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(White, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = Gray200,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        content()
    }
}

/**
 * Patient information card displaying avatar, name, IPP, and date of birth
 *
 * @param name Patient name
 * @param initials Name initials for avatar
 * @param ipp Patient IPP (ID)
 * @param dateOfBirth Patient date of birth
 * @param modifier Optional modifier
 */
@Composable
fun PatientCard(
    name: String,
    initials: String,
    ipp: String,
    dateOfBirth: String,
    modifier: Modifier = Modifier
) {
    BrancardageCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(0.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue100, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Blue600,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Info
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray900
                )
                Text(
                    text = "IPP: $ipp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Text(
                    text = "Né(e) le $dateOfBirth",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

/**
 * Text field component with label and optional icon
 *
 * @param value Current input value
 * @param onValueChange Callback when value changes
 * @param label Field label
 * @param placeholder Placeholder text
 * @param modifier Optional modifier
 * @param enabled Whether the field is enabled
 */
@Composable
fun BrancardageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Gray700
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Gray300,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(if (enabled) White else Gray100, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = if (enabled) onValueChange else { {} },
                modifier = Modifier
                    .padding(0.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = Gray900,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                singleLine = true,
                enabled = enabled,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

/**
 * Action menu card with icon and description
 *
 * @param title Action title
 * @param description Action description
 * @param onClick Callback when card is clicked
 * @param modifier Optional modifier
 * @param icon Optional icon composable
 */
@Composable
fun ActionMenuCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = Gray100,
                shape = RoundedCornerShape(12.dp)
            )
            .background(White, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Blue100, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

/**
 * Top app bar component
 *
 * @param title The title to display
 * @param onBackClick Callback for back button click (optional)
 * @param modifier Optional modifier
 */
@Composable
fun BrancardageTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Blue600)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1E40AF), RoundedCornerShape(50))
                    .clickable { onBackClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    color = White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

