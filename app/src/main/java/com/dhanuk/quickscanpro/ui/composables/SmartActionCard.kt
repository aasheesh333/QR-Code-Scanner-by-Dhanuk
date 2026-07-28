package com.dhanuk.quickscanpro.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector

data class SmartAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun SmartActionCard(
    action: SmartAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = action.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = action.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = action.color
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = action.color
            )
        }
    }
}

object SmartActionFactory {

    @Composable
    fun actionsForType(
        type: String,
        content: String,
        onOpen: () -> Unit,
        onCopy: () -> Unit,
        onShare: () -> Unit,
        onCall: () -> Unit,
        onEmail: () -> Unit,
        onSMS: () -> Unit,
        onAddContact: () -> Unit,
        onConnectWifi: () -> Unit,
        onAddCalendar: () -> Unit,
        onOpenMap: () -> Unit,
        onLookupProduct: () -> Unit
    ): List<SmartAction> {
        val baseActions = mutableListOf<SmartAction>()

        baseActions.add(
            SmartAction(
                "Copy to Clipboard", "Copy the content to copy buffer",
                Icons.Filled.ContentCopy, Color(0xFF6B7280), onCopy
            )
        )
        baseActions.add(
            SmartAction(
                "Share", "Share with other apps",
                Icons.Filled.Share, Color(0xFF3B82F6), onShare
            )
        )

        when (type) {
            BarcodeTypeDetector.TYPE_URL -> {
                baseActions.add(0, SmartAction(
                    "Open in Browser", "Launch the URL in browser",
                    Icons.Filled.OpenInNew, Color(0xFF10B981), onOpen
                ))
            }
            BarcodeTypeDetector.TYPE_EMAIL -> {
                baseActions.add(0, SmartAction(
                    "Compose Email", "Send email to this address",
                    Icons.Filled.Email, Color(0xFFEF4444), onEmail
                ))
                baseActions.add(1, SmartAction(
                    "Add to Contacts", "Save as contact",
                    Icons.Filled.PersonAdd, Color(0xFF8B5CF6), onAddContact
                ))
            }
            BarcodeTypeDetector.TYPE_PHONE -> {
                baseActions.add(0, SmartAction(
                    "Call", "Place a phone call",
                    Icons.Filled.Call, Color(0xFF10B981), onCall
                ))
                baseActions.add(1, SmartAction(
                    "Send SMS", "Start a text conversation",
                    Icons.Filled.Sms, Color(0xFF3B82F6), onSMS
                ))
                baseActions.add(2, SmartAction(
                    "Add to Contacts", "Save as contact",
                    Icons.Filled.PersonAdd, Color(0xFF8B5CF6), onAddContact
                ))
            }
            BarcodeTypeDetector.TYPE_SMS -> {
                baseActions.add(0, SmartAction(
                    "Send SMS", "Start the text message",
                    Icons.Filled.Sms, Color(0xFF3B82F6), onSMS
                ))
            }
            BarcodeTypeDetector.TYPE_WIFI -> {
                baseActions.add(0, SmartAction(
                    "Connect to WiFi", "Join this network",
                    Icons.Filled.Wifi, Color(0xFF8B5CF6), onConnectWifi
                ))
            }
            BarcodeTypeDetector.TYPE_VCARD -> {
                baseActions.add(0, SmartAction(
                    "Add to Contacts", "Save all contact details",
                    Icons.Filled.PersonAdd, Color(0xFF8B5CF6), onAddContact
                ))
            }
            BarcodeTypeDetector.TYPE_CALENDAR -> {
                baseActions.add(0, SmartAction(
                    "Add to Calendar", "Save this event",
                    Icons.Filled.Event, Color(0xFFF59E0B), onAddCalendar
                ))
            }
            BarcodeTypeDetector.TYPE_GEO -> {
                baseActions.add(0, SmartAction(
                    "Open in Maps", "View the location",
                    Icons.Filled.Map, Color(0xFF10B981), onOpenMap
                ))
            }
            BarcodeTypeDetector.TYPE_PRODUCT -> {
                baseActions.add(0, SmartAction(
                    "Look up Product", "Get product information",
                    Icons.Filled.ShoppingBag, Color(0xFFEF4444), onLookupProduct
                ))
            }
        }

        return baseActions
    }
}
