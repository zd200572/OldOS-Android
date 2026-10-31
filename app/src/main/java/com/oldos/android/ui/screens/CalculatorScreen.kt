package com.oldos.android.ui.screens

import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4CalcDigitGradient
import com.oldos.android.ui.theme.IOS4CalcFunctionGradient
import com.oldos.android.ui.theme.IOS4CalcLcdBackground
import com.oldos.android.ui.theme.IOS4CalcOperatorGradient
import com.oldos.android.ui.theme.IOS4ChromePressedOverlay
import com.oldos.android.ui.theme.IOS4FontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar

@Composable
fun CalculatorScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var shouldClearOnNextDigit by remember { mutableStateOf(false) }

    fun onDigit(d: String) {
        if (shouldClearOnNextDigit || displayValue == "0") {
            displayValue = d
            shouldClearOnNextDigit = false
        } else {
            if (displayValue.length < 9) {
                displayValue += d
            }
        }
    }

    fun onOperator(op: String) {
        val current = displayValue.toDoubleOrNull() ?: 0.0
        if (operand1 == null) {
            operand1 = current
        } else if (pendingOp != null && !shouldClearOnNextDigit) {
            val result = when (pendingOp) {
                "+" -> operand1!! + current
                "-" -> operand1!! - current
                "×" -> operand1!! * current
                "÷" -> if (current != 0.0) operand1!! / current else 0.0
                else -> current
            }
            operand1 = result
            displayValue = if (result % 1.0 == 0.0) "${result.toLong()}" else "$result"
        }
        pendingOp = op
        shouldClearOnNextDigit = true
    }

    fun onEquals() {
        if (operand1 != null && pendingOp != null) {
            val current = displayValue.toDoubleOrNull() ?: 0.0
            val result = when (pendingOp) {
                "+" -> operand1!! + current
                "-" -> operand1!! - current
                "×" -> operand1!! * current
                "÷" -> if (current != 0.0) operand1!! / current else 0.0
                else -> current
            }
            displayValue = if (result % 1.0 == 0.0) "${result.toLong()}" else "$result"
            operand1 = null
            pendingOp = null
            shouldClearOnNextDigit = true
        }
    }

    fun onClear() {
        displayValue = "0"
        operand1 = null
        pendingOp = null
        shouldClearOnNextDigit = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Calculator",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        // LCD Display Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .drawBehind {
                    // Glassy dark LCD: subtle vertical falloff + top sheen
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF050505), IOS4CalcLcdBackground, Color(0xFF232326))
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.12f
                        )
                    )
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = displayValue,
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                fontFamily = IOS4FontFamily,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        // Keypad Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.8f)
                .background(Color(0xFF222222))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val buttonRows = listOf(
                listOf("C" to KeyType.FUNCTION, "+/-" to KeyType.FUNCTION, "%" to KeyType.FUNCTION, "÷" to KeyType.OPERATOR),
                listOf("7" to KeyType.NUMBER, "8" to KeyType.NUMBER, "9" to KeyType.NUMBER, "×" to KeyType.OPERATOR),
                listOf("4" to KeyType.NUMBER, "5" to KeyType.NUMBER, "6" to KeyType.NUMBER, "-" to KeyType.OPERATOR),
                listOf("1" to KeyType.NUMBER, "2" to KeyType.NUMBER, "3" to KeyType.NUMBER, "+" to KeyType.OPERATOR),
                listOf("0" to KeyType.ZERO, "." to KeyType.NUMBER, "=" to KeyType.OPERATOR)
            )

            for (row in buttonRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for ((label, type) in row) {
                        CalcButton(
                            text = label,
                            type = type,
                            onClick = {
                                when (label) {
                                    "C" -> onClear()
                                    "+/-" -> {
                                        val v = displayValue.toDoubleOrNull() ?: 0.0
                                        val neg = -v
                                        displayValue = if (neg % 1.0 == 0.0) "${neg.toLong()}" else "$neg"
                                    }
                                    "%" -> {
                                        val v = (displayValue.toDoubleOrNull() ?: 0.0) / 100.0
                                        displayValue = "$v"
                                    }
                                    "÷", "×", "-", "+" -> onOperator(label)
                                    "=" -> onEquals()
                                    else -> onDigit(label)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class KeyType {
    NUMBER, OPERATOR, FUNCTION, ZERO
}

@Composable
private fun CalcButton(
    text: String,
    type: KeyType,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isZero = (type == KeyType.ZERO)
    val width = if (isZero) 152.dp else 70.dp
    val height = 66.dp

    val gradient = when (type) {
        KeyType.OPERATOR -> IOS4CalcOperatorGradient
        KeyType.FUNCTION -> IOS4CalcFunctionGradient
        else -> IOS4CalcDigitGradient
    }
    val textColor = if (type == KeyType.FUNCTION) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(brush = Brush.verticalGradient(gradient))
            .then(
                if (isPressed) Modifier.background(IOS4ChromePressedOverlay)
                else Modifier
            )
            .border(0.5.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Skeuomorphic glossy shine on top half of button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * 0.45f)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        Text(
            text = text,
            color = textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center
        )
    }
}
