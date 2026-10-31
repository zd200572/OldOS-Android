package com.oldos.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily
import com.oldos.android.ui.theme.IOS4RowValueText

import androidx.compose.ui.graphics.drawscope.Stroke

private data class MailMessage(
    val sender: String,
    val subject: String,
    val preview: String,
    val body: String,
    val date: String,
    val isUnread: Boolean = false
)

private val demoMailbox = listOf(
    MailMessage(
        "Apple Developer", "Your OldOS build is ready", "Great news — the archive for your",
        "Great news — the archive for your app has finished processing and is now ready for review.\n\nEverything from this build was assembled with care, the way it was in 2010.\n\n— The OldOS Team",
        "9:41 AM", isUnread = true
    ),
    MailMessage(
        "Zane Kleinberg", "Re: OldOS for Android", "Your port looks fantastic! The dock",
        "Your port looks fantastic! The dock reflection took me weeks to get right, and yours matches it almost exactly.\n\nIf you ever ship it, please keep the easter egg in Notes. Every good OS hides a letter.\n\n— Zane",
        "8:12 AM", isUnread = true
    ),
    MailMessage(
        "中国移动", "本月账单已出", "尊敬的客户，您 10 月的话费账单",
        "尊敬的客户，您 10 月的话费账单已生成。\n\n本月合计：￥58.00\n其中套餐费 ￥39.00，流量费 ￥19.00。\n\n感谢您长期以来的信任与支持。",
        "Yesterday", isUnread = true
    ),
    MailMessage(
        "Steve", "Design", "Simplicity is the ultimate sophistication.",
        "Simplicity is the ultimate sophistication.\n\nIt's not just about how it looks. Design is how it works.\n\nSent from my iPhone",
        "Yesterday"
    ),
    MailMessage(
        "Photos", "Memory: Golden hour, ten years ago", "You took 41 photos and 2 videos on",
        "You took 41 photos and 2 videos on this day, ten years ago.\n\nSome moments deserve a second look. Open Photos to relive them.\n\n— OldOS",
        "Friday"
    ),
    MailMessage(
        "App Store", "Weekly picks: vintage games", "This week we're celebrating the games",
        "This week we're celebrating the games that defined a decade — chunky pixels, glorious buttons and sounds you can feel.\n\nFeatured: Doom, Flatty Bird, Game Center classics.",
        "Friday"
    ),
    MailMessage(
        "com.oldos.android", "Welcome to OldOS", "Thanks for installing OldOS.",
        "Thanks for installing OldOS.\n\nEverything you remember is here: the glass dock, the linen textures, the little red badges.\n\nIt doesn't just look like 2010 — it keeps your real calls, texts, photos and music in that world.\n\nEnjoy the time machine.",
        "Oct 30"
    )
)

private data class Mailbox(val name: String, val count: Int, val icon: MailboxIcon)

private enum class MailboxIcon { INBOX, DRAFTS, SENT, TRASH }

/**
 * iOS 4 Mail: mailboxes → inbox → reader, with a working compose sheet that
 * hands off to the system mail client via mailto:. Content is a lovingly
 * written demo mailbox (real IMAP is out of scope).
 */
@Composable
fun MailScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var view by remember { mutableStateOf("mailboxes") }
    var openMessage by remember { mutableStateOf<MailMessage?>(null) }

    val backTarget: String = when (view) {
        "reader" -> "inbox"
        "inbox" -> "mailboxes"
        else -> "mailboxes"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = when (view) {
                "mailboxes" -> "Mailboxes"
                "inbox" -> "Inbox"
                else -> openMessage?.subject ?: ""
            },
            leftButtonText = if (view == "mailboxes") "Home" else backTarget.replaceFirstChar { it.uppercase() },
            isLeftBackButton = view != "mailboxes",
            onLeftButtonClick = {
                when (view) {
                    "mailboxes" -> onHomeClick()
                    "inbox" -> view = "mailboxes"
                    else -> view = "inbox"
                }
            },
            rightContent = if (view == "inbox") {
                { ComposeButton() }
            } else null
        )

        when (view) {
            "mailboxes" -> MailboxesList(
                onOpenInbox = { view = "inbox" }
            )
            "inbox" -> InboxList(onOpen = {
                openMessage = it
                view = "reader"
            })
            else -> openMessage?.let { Reader(message = it) }
        }
    }
}

@Composable
private fun ComposeButton() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.verticalGradient(com.oldos.android.ui.theme.IOS4NavButtonGradient))
            .clickable {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:?subject=Sent%20from%20OldOS"))
                )
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✎",
            color = Color.White,
            fontSize = 15.sp,
            fontFamily = IOS4FontFamily
        )
    }
}

@Composable
private fun MailboxesList(onOpenInbox: () -> Unit) {
    IOS4GroupCard(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenInbox() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MailboxGlyph(kind = MailboxIcon.INBOX, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Inbox",
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = IOS4FontFamily,
                modifier = Modifier.weight(1f)
            )
            // Unread count badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF3B78E7))
                    .padding(horizontal = 9.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "3",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IOS4ChevronGlyph(tint = Color(0xFF8F8F8F))
        }
        IOS4RowDivider()
        MailboxRow("Drafts", MailboxIcon.DRAFTS)
        IOS4RowDivider()
        MailboxRow("Sent", MailboxIcon.SENT)
        IOS4RowDivider()
        MailboxRow("Trash", MailboxIcon.TRASH)
    }

    IOS4GroupCard(
        footerText = "Mail is a faithful recreation with a demo mailbox. Composing opens your real mail client.",
        modifier = Modifier.padding(top = 10.dp)
    ) {}
}

@Composable
private fun MailboxRow(name: String, kind: MailboxIcon) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MailboxGlyph(kind = kind, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            color = Color(0xFF3E5383),
            fontSize = 17.sp,
            fontFamily = IOS4FontFamily,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InboxList(onOpen: (MailMessage) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(demoMailbox) { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { onOpen(message) }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unread dot
                if (message.isUnread) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E62DE))
                    )
                } else {
                    Spacer(modifier = Modifier.size(9.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.sender,
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = if (message.isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = IOS4FontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = message.date,
                            color = Color(0xFF3E5383),
                            fontSize = 12.sp,
                            fontFamily = IOS4FontFamily
                        )
                    }
                    Text(
                        text = message.subject,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = IOS4FontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = message.preview,
                        color = Color(0xFF8F8F8F),
                        fontSize = 13.sp,
                        fontFamily = IOS4FontFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

@Composable
private fun Reader(message: MailMessage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Message header card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.subject,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = "From:",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = IOS4FontFamily,
                    modifier = Modifier.width(52.dp)
                )
                Text(
                    text = message.sender,
                    color = IOS4RowValueText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = IOS4FontFamily
                )
            }
            Row(modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = "To:",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = IOS4FontFamily,
                    modifier = Modifier.width(52.dp)
                )
                Text(text = "me@oldos.local", color = IOS4RowValueText, fontSize = 13.sp, fontFamily = IOS4FontFamily)
            }
            Row(modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = "Date:",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontFamily = IOS4FontFamily,
                    modifier = Modifier.width(52.dp)
                )
                Text(text = message.date, color = IOS4RowValueText, fontSize = 13.sp, fontFamily = IOS4FontFamily)
            }
        }

        // Body
        Text(
            text = message.body,
            color = Color(0xFF1A1A1A),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = IOS4FontFamily,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        )

        Text(
            text = "— Sent from OldOS on a phone from 2010",
            color = Color(0xFF8F8F8F),
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = IOS4FontFamily,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

/** Little folder glyphs for the mailboxes list. */
@Composable
private fun MailboxGlyph(kind: MailboxIcon, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val blue = Color(0xFF5AA0E8)
        when (kind) {
            MailboxIcon.INBOX -> {
                // Tray with arrow
                drawRect(blue, topLeft = Offset(0f, size.height * 0.55f), size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.45f))
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.5f, 0f)
                    lineTo(size.width * 0.5f, size.height * 0.5f)
                }
                drawPath(
                    path, color = blue,
                    style = Stroke(width = size.width * 0.14f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                val head = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.32f)
                    lineTo(size.width * 0.5f, size.height * 0.52f)
                    lineTo(size.width * 0.7f, size.height * 0.32f)
                }
                drawPath(
                    head, color = blue,
                    style = Stroke(width = size.width * 0.14f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )
            }
            else -> {
                // Folder
                drawRoundRect(
                    blue,
                    topLeft = Offset(0f, size.height * 0.25f),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.7f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                drawRoundRect(
                    blue.copy(alpha = 0.75f),
                    topLeft = Offset(0f, size.height * 0.15f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.45f, size.height * 0.2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }
        }
    }
}
