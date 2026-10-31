package com.oldos.android.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.oldos.android.data.RealContact
import com.oldos.android.data.SystemDataRepository
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

import com.oldos.android.ui.theme.IOS4NavButtonBlueGradient
import com.oldos.android.ui.theme.IOS4RowValueText

/**
 * iOS 4 Contacts: alphabetical list with red section letters and a detail
 * page. Tapping a number dials it through the system dialer.
 */
@Composable
fun ContactsScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedContact by remember { mutableStateOf<RealContact?>(null) }
    var contacts by remember { mutableStateOf<List<RealContact>>(emptyList()) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            contacts = SystemDataRepository.readContacts(context)
        }
    }

    if (selectedContact != null) {
        ContactDetailScreen(
            contact = selectedContact!!,
            onBack = { selectedContact = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Contacts",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Contacts access is required to show your address book.",
                        color = Color(0xFF4C566C),
                        fontSize = 14.sp,
                        fontFamily = IOS4FontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                            .background(Brush.verticalGradient(IOS4NavButtonBlueGradient))
                            .clickable { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "Grant Access",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IOS4FontFamily
                        )
                    }
                }
            }
        } else if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No Contacts",
                    color = Color(0xFF8F8F8F),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        } else {
            // Alphabetical sections with iOS 4 red-letter headers
            val grouped = remember(contacts) {
                contacts.sortedBy { it.name.uppercase() }
                    .groupBy { contact ->
                        contact.name.firstOrNull()?.uppercaseChar()
                            ?.takeIf { it in 'A'..'Z' } ?: '#'
                    }
                    .toSortedMap(compareBy { it })
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (letter, group) ->
                    item(key = "header_$letter") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFC5CCD4))
                                .padding(horizontal = 14.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = letter.toString(),
                                color = Color(0xFF50555C),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = IOS4FontFamily
                            )
                        }
                    }
                    items(group.size, key = { group[it].contactId + it }) { index ->
                        val contact = group[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .clickable { selectedContact = contact }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = contact.name,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontFamily = IOS4FontFamily
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color(0xFFE2E2E2))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactDetailScreen(
    contact: RealContact,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = contact.name,
            leftButtonText = "Contacts",
            isLeftBackButton = true,
            onLeftButtonClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            IOS4GroupCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Initials avatar
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8EA6C4), Color(0xFF476999))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .take(2)
                                .joinToString(""),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IOS4FontFamily
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = contact.name,
                        color = Color.Black,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IOS4FontFamily
                    )
                }
            }

            IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
                IOS4RowItem(title = contact.numberType.replaceFirstChar { it.uppercase() })
                IOS4RowDivider()
                IOS4RowItem(
                    title = contact.number,
                    titleColor = IOS4RowValueText,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}")))
                    }
                )
            }

            IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
                IOS4RowItem(
                    title = "Send Message",
                    titleColor = IOS4RowValueText,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}")))
                    }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "FaceTime",
                    detailText = "Unavailable on OldOS"
                )
            }
        }
    }
}
