package com.oldos.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily
import java.io.File

/**
 * iOS 4 Notes: pinstripe list + yellow ruled-paper editor.
 * Notes persist as plain text files in the app's files directory.
 */
object NotesRepository {
    private fun notesDir(context: android.content.Context): File {
        val dir = File(context.filesDir, "notes").apply { mkdirs() }
        return dir
    }

    fun loadNotes(context: android.content.Context): List<File> =
        notesDir(context).listFiles { f -> f.extension == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    fun saveNote(context: android.content.Context, name: String, content: String): File {
        val file = File(notesDir(context), "$name.txt")
        file.writeText(content)
        return file
    }

    fun deleteNote(context: android.content.Context, name: String) {
        File(notesDir(context), "$name.txt").delete()
    }

    fun titleOf(file: File): String =
        file.nameWithoutExtension

    fun previewOf(file: File): String =
        file.readLines().drop(1).firstOrNull { it.isNotBlank() } ?: "No additional text"
}

private const val NOTE_MAX_NAME = 40

@Composable
fun NotesScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var notes by remember { mutableStateOf(NotesRepository.loadNotes(context)) }
    var openNote by remember { mutableStateOf<File?>(null) }

    if (openNote != null) {
        NoteEditor(
            noteFile = openNote!!,
            onClose = {
                notes = NotesRepository.loadNotes(context)
                openNote = null
            },
            onDelete = {
                NotesRepository.deleteNote(context, openNote!!.nameWithoutExtension)
                notes = NotesRepository.loadNotes(context)
                openNote = null
            }
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
            title = "Notes",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick,
            rightButtonText = "+",
            onRightButtonClick = {
                val newName = "Note ${(notes.size + 1)}"
                NotesRepository.saveNote(context, newName, "New Note\n")
                notes = NotesRepository.loadNotes(context)
                openNote = notes.firstOrNull { it.nameWithoutExtension == newName }
            }
        )

        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No Notes",
                    color = Color(0xFF8F8F8F),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(notes) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clickable { openNote = note }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small paper icon
                        Canvas(modifier = Modifier.size(width = 30.dp, height = 24.dp)) {
                            drawRect(color = Color(0xFFFBF5C8))
                            for (i in 1..3) {
                                drawLine(
                                    color = Color(0xFFB8CBDD),
                                    start = Offset(0f, size.height * i / 4f),
                                    end = Offset(size.width, size.height * i / 4f),
                                    strokeWidth = 1f
                                )
                            }
                            drawLine(
                                color = Color(0xFFD8C98A),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 2.5f
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = NotesRepository.titleOf(note),
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontFamily = IOS4FontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = NotesRepository.previewOf(note),
                                color = Color(0xFF8F8F8F),
                                fontSize = 13.sp,
                                fontFamily = IOS4FontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IOS4ChevronGlyph(tint = Color(0xFF8F8F8F))
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

@Composable
private fun NoteEditor(
    noteFile: File,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf(noteFile.readText()) }

    Column(modifier = Modifier.fillMaxSize()) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = NotesRepository.titleOf(noteFile),
            leftButtonText = "Notes",
            isLeftBackButton = true,
            onLeftButtonClick = {
                NotesRepository.saveNote(context, noteFile.nameWithoutExtension, content)
                onClose()
            },
            rightButtonText = "Delete",
            onRightButtonClick = onDelete
        )

        // Yellow ruled paper
        BoxWithRuledLines(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = content,
                onValueChange = {
                    content = it
                    NotesRepository.saveNote(context, noteFile.nameWithoutExtension, it)
                },
                textStyle = TextStyle(
                    color = Color(0xFF1A1A1A),
                    fontSize = 16.sp,
                    fontFamily = IOS4FontFamily,
                    lineHeight = 28.sp
                ),
                cursorBrush = SolidColor(Color(0xFF3B78E7)),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

/** Yellow legal-pad background: cream fill, blue rules, red margin line. */
@Composable
private fun BoxWithRuledLines(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.background(Color(0xFFFBF5C8))) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val lineStep = 28.sp.toPx()
            var y = lineStep * 1.6f
            while (y < size.height) {
                drawLine(
                    color = Color(0xFFC3D3E3),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.2f
                )
                y += lineStep
            }
            drawLine(
                color = Color(0xFFE4A29A),
                start = Offset(size.width * 0.06f, 0f),
                end = Offset(size.width * 0.06f, size.height),
                strokeWidth = 2.2f
            )
        }
        content()
    }
}
