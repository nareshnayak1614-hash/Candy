package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreSchema
import com.example.ui.theme.BrandNavy900
import com.example.ui.theme.BrandNavy700
import com.example.ui.theme.SafetyBluePrimary

@Composable
fun SetupGuideDialog(
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.IntegrationInstructions,
                    contentDescription = null,
                    tint = SafetyBluePrimary
                )
                Text("Firebase & Architecture Guide")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Firestore Rules") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Collections") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Deployment") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        Text(
                            text = "Zero-Trust Security Rules for Firestore:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandNavy900)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = FirestoreSchema.FIRESTORE_RULES,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(FirestoreSchema.FIRESTORE_RULES))
                                copiedText = "Copied Rules to Clipboard!"
                            },
                            modifier = Modifier.fillMaxWidth().testTag("copy_rules_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(copiedText ?: "Copy Firestore Rules")
                        }
                    }
                    1 -> {
                        Text(
                            text = "Cloud Firestore Database Structure:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📁 parents/{parentId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("  ├── 📄 profile (email, name, pinHash)", fontSize = 11.sp)
                                Text("  └── 📁 devices/{deviceId} (Up to 10 connected)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("        ├── 📄 state (battery, wifi, isLocked, activeApp)", fontSize = 11.sp)
                                Text("        ├── 📄 screentime (limits, schedules, appUsage)", fontSize = 11.sp)
                                Text("        ├── 📁 locations (realtime GPS, breadcrumbs)", fontSize = 11.sp)
                                Text("        └── 📁 sessions (camera/mic/screen WebRTC signaling)", fontSize = 11.sp)
                                Text("📁 pairing_codes/{code}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("  └── 📄 {code} -> {parentId, deviceId, expiresAt}", fontSize = 11.sp)
                            }
                        }
                    }
                    2 -> {
                        Text(
                            text = "Step-by-Step Production Deployment:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = FirestoreSchema.SETUP_INSTRUCTIONS,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_guide_button")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
