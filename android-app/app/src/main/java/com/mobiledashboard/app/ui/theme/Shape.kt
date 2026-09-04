package com.mobiledashboard.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ==============================================================================
// MATERIAL 3 (M3 EXPRESSIVE) SHAPE SPECIFICATIONS & TOKENS
// ==============================================================================

val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Granular tokens for custom cards & widget elements
object M3ShapeTokens {
    val Chip = RoundedCornerShape(10.dp)
    val Pill = RoundedCornerShape(percent = 50)
    val SubCard = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(28.dp)
    val HeroCard = RoundedCornerShape(32.dp)
    val FullPlayer = RoundedCornerShape(36.dp)
    val Circle = CircleShape
    val AsymmetricCard = RoundedCornerShape(
        topStart = 36.dp,
        topEnd = 14.dp,
        bottomEnd = 36.dp,
        bottomStart = 14.dp
    )
    val ReverseAsymmetricCard = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 36.dp,
        bottomEnd = 14.dp,
        bottomStart = 36.dp
    )
    val LeafCard = RoundedCornerShape(
        topStart = 36.dp,
        topEnd = 12.dp,
        bottomEnd = 36.dp,
        bottomStart = 12.dp
    )
    val DiagonalCard = RoundedCornerShape(
        topStart = 40.dp,
        topEnd = 14.dp,
        bottomEnd = 40.dp,
        bottomStart = 14.dp
    )
    val ReverseDiagonalCard = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 40.dp,
        bottomEnd = 14.dp,
        bottomStart = 40.dp
    )
    val DiagonalPill = RoundedCornerShape(
        topStart = 48.dp,
        topEnd = 14.dp,
        bottomEnd = 48.dp,
        bottomStart = 14.dp
    )
    val ReverseDiagonalPill = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 48.dp,
        bottomEnd = 14.dp,
        bottomStart = 48.dp
    )
    val Scalloped8 = M3ScallopedShape(8)

    fun getShape(style: String?, defaultShape: Shape = Card): Shape {
        return when (style) {
            "pill" -> Pill
            "scalloped", "flower" -> M3ScallopedShape(8)
            "clover" -> M3CloverShape()
            "asymmetric" -> AsymmetricCard
            "subcard" -> SubCard
            "circle" -> Circle
            else -> defaultShape
        }
    }
}

/**
 * Android 14/15 Google Pixel 4-Lobe Clover Widget Shape.
 */
class M3CloverShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val width = size.width
        val height = size.height
        val minDim = min(width, height)
        val cornerRadius = minDim * 0.38f

        // Rounded 4-corner clover polygon
        path.addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = Rect(0f, 0f, width, height),
                topLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                topRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                bottomRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                bottomLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
        )
        return Outline.Generic(path)
    }
}

/**
 * Android 14/15 Google Pixel 8-Scalloped Organic Dial Shape.
 */
class M3ScallopedShape(private val lobes: Int = 8) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = min(centerX, centerY)
        val scallopDepth = radius * 0.12f

        val points = 360
        for (i in 0 until points) {
            val angle = i * (PI / 180.0)
            val r = radius - scallopDepth * (0.5f + 0.5f * cos(lobes * angle).toFloat())
            val x = centerX + (r * cos(angle)).toFloat()
            val y = centerY + (r * sin(angle)).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}
