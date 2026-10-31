package com.oldos.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily

private data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,      // absolute
    val changePercent: Double,
    val trend: List<Float>   // sparkline points 0..1
)

// Demo data — clearly labeled in the UI
private val stocks = listOf(
    Stock("SH COMP", "Shanghai Composite", 3245.16, -12.48, -0.38, listOf(0.4f, 0.45f, 0.52f, 0.48f, 0.55f, 0.6f, 0.58f, 0.5f, 0.44f, 0.42f)),
    Stock("HSI", "Hang Seng Index", 24680.33, 210.55, 0.86, listOf(0.3f, 0.35f, 0.32f, 0.4f, 0.45f, 0.52f, 0.5f, 0.6f, 0.66f, 0.7f)),
    Stock("DJI", "Dow Jones Industrials", 34386.32, 156.22, 0.46, listOf(0.5f, 0.55f, 0.5f, 0.58f, 0.6f, 0.57f, 0.63f, 0.62f, 0.68f, 0.72f)),
    Stock("NASDAQ", "Nasdaq Composite", 13789.11, -45.90, -0.33, listOf(0.7f, 0.66f, 0.62f, 0.65f, 0.58f, 0.55f, 0.52f, 0.56f, 0.5f, 0.47f)),
    Stock("AAPL", "Apple Inc.", 421.55, 6.32, 1.52, listOf(0.3f, 0.34f, 0.4f, 0.44f, 0.42f, 0.5f, 0.56f, 0.6f, 0.58f, 0.65f)),
    Stock("GOOG", "Alphabet Inc.", 133.72, -1.44, -1.07, listOf(0.6f, 0.58f, 0.6f, 0.54f, 0.5f, 0.48f, 0.44f, 0.46f, 0.42f, 0.4f))
)

/**
 * iOS 4 Stocks: dark navy list with green/red change badges and a chart
 * detail. Values are static demo data (the era's live Yahoo feed is gone).
 */
@Composable
fun StocksScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf<Stock?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3E4A5C), Color(0xFF12161E))
                )
            )
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Stocks",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        if (selected != null) {
            StockDetail(stock = selected!!, onBack = { selected = null })
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(stocks) { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selected = stock }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = stock.symbol,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = IOS4FontFamily
                            )
                            Text(
                                text = stock.name,
                                color = Color(0xFF9AA7B8),
                                fontSize = 12.sp,
                                fontFamily = IOS4FontFamily,
                                maxLines = 1
                            )
                        }
                        MiniSparkline(
                            trend = stock.trend,
                            up = stock.change >= 0,
                            modifier = Modifier.weight(1f).height(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.9f)) {
                            Text(
                                text = String.format("%,.2f", stock.price),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = IOS4FontFamily
                            )
                            val up = stock.change >= 0
                            Text(
                                text = "${if (up) "+" else ""}${String.format("%.2f", stock.changePercent)}%",
                                color = if (up) Color(0xFF7DE640) else Color(0xFFFF6B60),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = IOS4FontFamily
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0x33FFFFFF))
                    )
                }
                item {
                    Text(
                        text = "Demo data — the 2010 Yahoo! Finance feed is long gone.",
                        color = Color(0x66FFFFFF),
                        fontSize = 11.sp,
                        fontFamily = IOS4FontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StockDetail(stock: Stock, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onBack() }
            .padding(12.dp)
    ) {
        Text(
            text = stock.symbol,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = IOS4FontFamily
        )
        Text(
            text = "${String.format("%,.2f", stock.price)}  ${if (stock.change >= 0) "+" else ""}${String.format("%.2f", stock.change)} (${String.format("%.2f", stock.changePercent)}%)",
            color = if (stock.change >= 0) Color(0xFF7DE640) else Color(0xFFFF6B60),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = IOS4FontFamily,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Big chart with grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x22000000))
                .padding(10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Grid
                val gridColor = Color.White.copy(alpha = 0.08f)
                for (i in 1..3) {
                    drawLine(gridColor, Offset(0f, size.height * i / 4f), Offset(size.width, size.height * i / 4f), strokeWidth = 1f)
                    drawLine(gridColor, Offset(size.width * i / 4f, 0f), Offset(size.width * i / 4f, size.height), strokeWidth = 1f)
                }

                val points = stock.trend
                if (points.size > 1) {
                    val chartColor = if (stock.change >= 0) Color(0xFF7DE640) else Color(0xFFFF6B60)
                    val stepX = size.width / (points.size - 1)
                    val path = Path()
                    val fill = Path()
                    points.forEachIndexed { i, p ->
                        val x = stepX * i
                        val y = size.height * (1f - p) * 0.92f + size.height * 0.04f
                        if (i == 0) {
                            path.moveTo(x, y)
                            fill.moveTo(x, size.height)
                            fill.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fill.lineTo(x, y)
                        }
                    }
                    fill.lineTo(size.width, size.height)
                    fill.close()
                    drawPath(fill, brush = Brush.verticalGradient(
                        listOf(chartColor.copy(alpha = 0.30f), Color.Transparent)
                    ))
                    drawPath(path, color = chartColor, style = Stroke(width = 2.4.dp.toPx()))
                }
            }
        }

        // Stats grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailStat("Open", String.format("%,.2f", stock.price - stock.change))
            DetailStat("Chg", String.format("%+.2f", stock.change))
            DetailStat("% Chg", String.format("%+.2f%%", stock.changePercent))
            DetailStat("Range", "${String.format("%.0f", stock.price * 0.99)}–${String.format("%.0f", stock.price * 1.01)}")
        }

        Text(
            text = "Tap anywhere to go back",
            color = Color(0x66FFFFFF),
            fontSize = 11.sp,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun DetailStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color(0xFF9AA7B8),
            fontSize = 11.sp,
            fontFamily = IOS4FontFamily
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = IOS4FontFamily
        )
    }
}

@Composable
private fun MiniSparkline(trend: List<Float>, up: Boolean, modifier: Modifier = Modifier) {
    val color = if (up) Color(0xFF7DE640) else Color(0xFFFF6B60)
    Canvas(modifier = modifier) {
        if (trend.size < 2) return@Canvas
        val stepX = size.width / (trend.size - 1)
        val path = Path()
        trend.forEachIndexed { i, p ->
            val x = stepX * i
            val y = size.height * (1f - p) * 0.86f + size.height * 0.07f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = color, style = Stroke(width = 1.8.dp.toPx()))
    }
}
