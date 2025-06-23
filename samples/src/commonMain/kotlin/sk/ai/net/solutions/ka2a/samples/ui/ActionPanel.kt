package sk.ai.net.solutions.ka2a.samples.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPanel(
    isConnected: Boolean,
    onPingClicked: () -> Unit,
    onDiscoverClicked: () -> Unit,
    onTranslateClicked: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "A2A Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Basic actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = onPingClicked,
                    enabled = isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ping")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onDiscoverClicked,
                    enabled = isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Discover")
                }
            }
            
            // Translation section
            Text(
                text = "Translation",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            
            var text by remember { mutableStateOf("Hello, world!") }
            var expanded by remember { mutableStateOf(false) }
            var selectedLanguage by remember { mutableStateOf("es") }
            
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text to translate") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                enabled = isConnected
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (isConnected) expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = when (selectedLanguage) {
                        "es" -> "Spanish"
                        "fr" -> "French"
                        "de" -> "German"
                        "it" -> "Italian"
                        "ja" -> "Japanese"
                        else -> "Spanish"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = isConnected
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Spanish") },
                        onClick = {
                            selectedLanguage = "es"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("French") },
                        onClick = {
                            selectedLanguage = "fr"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("German") },
                        onClick = {
                            selectedLanguage = "de"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Italian") },
                        onClick = {
                            selectedLanguage = "it"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Japanese") },
                        onClick = {
                            selectedLanguage = "ja"
                            expanded = false
                        }
                    )
                }
            }
            
            Button(
                onClick = { onTranslateClicked(text, selectedLanguage) },
                enabled = isConnected && text.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Translate")
            }
        }
    }
}