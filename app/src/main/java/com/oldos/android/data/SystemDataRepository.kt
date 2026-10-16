package com.oldos.android.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

// ──────────────────────────────────────────────────────────────────────────────
// Data models
// ──────────────────────────────────────────────────────────────────────────────

data class RealCallEntry(
    val id: String,
    val name: String,      // Contact name or raw number
    val number: String,
    val dateMs: Long,
    val callType: Int,     // CallLog.Calls.TYPE: 1=in, 2=out, 3=missed, 4=voicemail
    val durationSec: Long,
    val timeDisplay: String,
    val isMissed: Boolean
)

data class RealContact(
    val contactId: String,
    val name: String,
    val number: String,
    val numberType: String   // mobile / work / home …
)

data class RealSmsThread(
    val threadId: String,
    val contactName: String,
    val phoneNumber: String,
    val lastBody: String,
    val lastDateMs: Long,
    val dateDisplay: String,
    var isUnread: Boolean,
    val messages: MutableList<RealSmsMessage>
)

data class RealSmsMessage(
    val id: String,
    val body: String,
    val dateMs: Long,
    val isFromMe: Boolean,   // type=1→inbox (received), type=2→sent
    val timestamp: String
)

// ──────────────────────────────────────────────────────────────────────────────
// Repository
// ──────────────────────────────────────────────────────────────────────────────

object SystemDataRepository {

    private const val TAG = "SystemDataRepository"
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

    /** Format a timestamp in millis to a human-friendly display string */
    fun formatDate(ms: Long): String {
        if (ms <= 0) return ""
        val cal = Calendar.getInstance().apply { timeInMillis = ms }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(cal, today) -> timeFmt.format(Date(ms))
            isSameDay(cal, yesterday) -> "Yesterday"
            today.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR)
                    && today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) -> {
                SimpleDateFormat("EEE", Locale.getDefault()).format(Date(ms))
            }
            else -> SimpleDateFormat("M/d", Locale.getDefault()).format(Date(ms))
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    // ── Call Log ──────────────────────────────────────────────────────────────

    /**
     * Read up to [limit] recent calls from CallLog.Calls.
     * Uses safe cursor iteration without SQL LIMIT keyword to avoid SQLite errors on modern Android.
     */
    fun readCallLog(context: Context, limit: Int = 100): List<RealCallEntry> {
        val results = mutableListOf<RealCallEntry>()
        val cr: ContentResolver = context.contentResolver

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        try {
            // Note: sortOrder without LIMIT keyword avoids IllegalArgumentException on modern ROMs
            cr.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CallLog.Calls._ID)
                val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION)
                val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)

                var count = 0
                while (cursor.moveToNext() && count < limit) {
                    val id = if (idIdx >= 0) cursor.getString(idIdx) ?: UUID.randomUUID().toString() else UUID.randomUUID().toString()
                    val rawName = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                    val number = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                    val dateMs = if (dateIdx >= 0) cursor.getLong(dateIdx) else System.currentTimeMillis()
                    val duration = if (durIdx >= 0) cursor.getLong(durIdx) else 0L
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE

                    val displayName = when {
                        !rawName.isNullOrBlank() -> rawName
                        number.isNotBlank() -> number
                        else -> "未知号码"
                    }

                    results += RealCallEntry(
                        id = id,
                        name = displayName,
                        number = number,
                        dateMs = dateMs,
                        callType = type,
                        durationSec = duration,
                        timeDisplay = formatDate(dateMs),
                        isMissed = type == CallLog.Calls.MISSED_TYPE
                    )
                    count++
                }
            }
            Log.i(TAG, "Successfully loaded ${results.size} call log records")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query CallLog.Calls", e)
        }

        return results
    }

    // ── Contacts ──────────────────────────────────────────────────────────────

    /**
     * Read all phone contacts.
     */
    fun readContacts(context: Context): List<RealContact> {
        val results = mutableListOf<RealContact>()
        val cr: ContentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE
        )

        try {
            cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                val seen = mutableSetOf<String>()

                while (cursor.moveToNext()) {
                    val contactId = if (idIdx >= 0) cursor.getString(idIdx) ?: continue else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: continue else continue
                    val number = if (numIdx >= 0) cursor.getString(numIdx) ?: continue else continue
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE

                    val key = "$contactId|$number"
                    if (seen.add(key)) {
                        val typeLabel = when (type) {
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "mobile"
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "work"
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "home"
                            else -> "other"
                        }
                        results += RealContact(
                            contactId = contactId,
                            name = name,
                            number = number,
                            numberType = typeLabel
                        )
                    }
                }
            }
            Log.i(TAG, "Successfully loaded ${results.size} contacts")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query Contacts", e)
        }

        return results
    }

    // ── SMS ───────────────────────────────────────────────────────────────────

    /**
     * Look up a contact display name for a phone number.
     */
    fun lookupContactName(cr: ContentResolver, number: String): String? {
        if (number.isBlank()) return null
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        return try {
            cr.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val colIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                        if (colIdx >= 0) cursor.getString(colIdx) else null
                    } else null
                }
        } catch (_: Exception) { null }
    }

    /**
     * Read SMS messages in a single fast, robust query and group by thread_id or address.
     * Never uses SQL LIMIT keywords that fail on TelephonyProvider.
     */
    fun readSmsThreads(
        context: Context,
        maxTotalMessages: Int = 400
    ): List<RealSmsThread> {
        val cr: ContentResolver = context.contentResolver

        // Intermediate holder for messages per thread key
        class RawMessage(
            val id: String,
            val threadId: String,
            val address: String,
            val body: String,
            val dateMs: Long,
            val isFromMe: Boolean,
            val isUnread: Boolean
        )

        val rawList = mutableListOf<RawMessage>()

        val urisToTry = listOf(
            Uri.parse("content://sms"),
            Uri.parse("content://sms/inbox"),
            android.provider.Telephony.Sms.CONTENT_URI
        )

        for (uri in urisToTry) {
            try {
                cr.query(
                    uri,
                    null,
                    null,
                    null,
                    "date DESC"
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex("_id")
                    val tidIdx = cursor.getColumnIndex("thread_id")
                    val addrIdx = cursor.getColumnIndex("address")
                    val bodyIdx = cursor.getColumnIndex("body")
                    val dateIdx = cursor.getColumnIndex("date")
                    val typeIdx = cursor.getColumnIndex("type")
                    val readIdx = cursor.getColumnIndex("read")

                    var count = 0
                    while (cursor.moveToNext() && count < maxTotalMessages) {
                        val id = if (idIdx >= 0) cursor.getString(idIdx) ?: UUID.randomUUID().toString() else UUID.randomUUID().toString()
                        val tid = if (tidIdx >= 0) cursor.getString(tidIdx) ?: "" else ""
                        val addr = if (addrIdx >= 0) cursor.getString(addrIdx) ?: "" else ""
                        val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                        val dateMs = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                        val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else 1
                        val read = if (readIdx >= 0) cursor.getInt(readIdx) else 1

                        if (body.isNotBlank() || addr.isNotBlank()) {
                            rawList += RawMessage(
                                id = id,
                                threadId = if (tid.isNotBlank() && tid != "0") tid else addr,
                                address = addr,
                                body = body,
                                dateMs = dateMs,
                                isFromMe = (type == 2),
                                isUnread = (read == 0 && type == 1)
                            )
                            count++
                        }
                    }
                }
                Log.i(TAG, "Queried $uri -> found ${rawList.size} SMS messages")
                if (rawList.isNotEmpty()) break
            } catch (e: Exception) {
                Log.w(TAG, "Querying $uri failed: ${e.message}")
            }
        }

        if (rawList.isEmpty()) return emptyList()

        // Cache contact name lookups so we don't query repeatedly for the same address
        val contactNameCache = mutableMapOf<String, String>()

        // Group by thread key preserving most recent order
        val grouped = linkedMapOf<String, MutableList<RawMessage>>()
        for (msg in rawList) {
            val key = if (msg.threadId.isNotBlank()) msg.threadId else msg.address
            grouped.getOrPut(key) { mutableListOf() }.add(msg)
        }

        val threads = mutableListOf<RealSmsThread>()

        for ((threadKey, messagesDesc) in grouped) {
            val latest = messagesDesc.first()
            val phone = messagesDesc.firstOrNull { it.address.isNotBlank() }?.address ?: threadKey

            val contactName = contactNameCache.getOrPut(phone) {
                lookupContactName(cr, phone) ?: if (phone.isNotBlank()) phone else "未知发件人"
            }

            val hasUnread = messagesDesc.any { it.isUnread }

            // Chronological order for inside the chat view
            val chatMessages = messagesDesc.reversed().map {
                RealSmsMessage(
                    id = it.id,
                    body = it.body,
                    dateMs = it.dateMs,
                    isFromMe = it.isFromMe,
                    timestamp = timeFmt.format(Date(it.dateMs))
                )
            }.toMutableList()

            threads += RealSmsThread(
                threadId = threadKey,
                contactName = contactName,
                phoneNumber = phone,
                lastBody = latest.body,
                lastDateMs = latest.dateMs,
                dateDisplay = formatDate(latest.dateMs),
                isUnread = hasUnread,
                messages = chatMessages
            )
        }

        Log.i(TAG, "Successfully built ${threads.size} conversation threads")
        return threads
    }
}
