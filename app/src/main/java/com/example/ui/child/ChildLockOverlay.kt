package com.example.ui.child

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandNavy900
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SafetyBluePrimary

@Composable
fun ChildLockOverlay(
    childName: String,
    lockReason: String,
    onParentUnlockClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandNavy900)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Device Locked",
                    tint = EmergencyRed,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Device Paused",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (lockReason.isNotBlank()) lockReason else "This device is locked by your parents for bedtime or focus time.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Calls Allowed (Safety requirement)
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:911")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.85f).testTag("emergency_call_button")
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Call (911)", fontWeight = FontWeight.SemiBold)
            }

            // Parent PIN Unlock
            Button(
                onClick = onParentUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyBluePrimary),
                modifier = Modifier.fillMaxWidth(0.85f).testTag("parent_pin_unlock_button")
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Parent PIN Unlock")
            }
        }
    }
}
