package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.ui.formatWeight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws a graph node with metabolite information.
 */
fun DrawScope.drawGraphNode(
    position: NodePosition,
    metabolites: List<Metabolite>,
    colorScheme: ColorScheme,
    textMeasurer: TextMeasurer
) {
    val topLeft = Offset(position.x, position.y)
    val size = Size(position.width, position.height)

    // Draw background with node color (lighter version)
    drawRoundRect(
        color = position.color.copy(alpha = 0.2f),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Draw border with node color
    drawRoundRect(
        color = position.color,
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 3f)
    )

    // Draw organ name (header)
    val headerStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onPrimaryContainer
    )

    val headerResult = textMeasurer.measure(
        text = position.organName,
        style = headerStyle
    )

    drawText(
        textLayoutResult = headerResult,
        topLeft = Offset(
            x = position.x + GraphLayoutEngine.NODE_PADDING,
            y = position.y + GraphLayoutEngine.NODE_PADDING
        )
    )

    // Draw metabolites
    var currentY = position.y + GraphLayoutEngine.NODE_PADDING + headerResult.size.height + 8f

    val metaboliteStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = colorScheme.onPrimaryContainer
    )

    val metaboliteValueStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = colorScheme.onPrimaryContainer
    )

    metabolites.forEach { metabolite ->
        // Draw metabolite type
        val typeResult = textMeasurer.measure(
            text = metabolite.type.toString(),
            style = metaboliteStyle
        )

        drawText(
            textLayoutResult = typeResult,
            topLeft = Offset(
                x = position.x + GraphLayoutEngine.NODE_PADDING,
                y = currentY
            )
        )

        // Draw metabolite amount
        val amountText = formatWeight(metabolite.amountMilliMoles)
        val amountResult = textMeasurer.measure(
            text = amountText,
            style = metaboliteValueStyle
        )

        drawText(
            textLayoutResult = amountResult,
            topLeft = Offset(
                x = position.x + position.width - GraphLayoutEngine.NODE_PADDING - amountResult.size.width,
                y = currentY
            )
        )

        currentY += GraphLayoutEngine.LINE_HEIGHT
    }

    // If no metabolites, show message
    if (metabolites.isEmpty()) {
        val emptyStyle = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )

        val emptyResult = textMeasurer.measure(
            text = "No metabolites",
            style = emptyStyle
        )

        drawText(
            textLayoutResult = emptyResult,
            topLeft = Offset(
                x = position.x + GraphLayoutEngine.NODE_PADDING,
                y = currentY
            )
        )
    }
}

/**
 * Draws an edge between nodes using orthogonal routing.
 */
fun DrawScope.drawGraphEdge(
    edgePath: EdgePath,
    colorScheme: ColorScheme,
    textMeasurer: TextMeasurer
) {
    val color = edgePath.color // Use the edge's assigned color (matches source node)

    val waypoints = edgePath.waypoints
    if (waypoints.size < 2) return

    // Draw orthogonal path through waypoints (all solid lines)
    for (i in 0 until waypoints.size - 1) {
        val start = waypoints[i]
        val end = waypoints[i + 1]

        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = 3f
        )
    }

    // Draw arrowhead at the end
    val lastSegmentStart = waypoints[waypoints.size - 2]
    val lastSegmentEnd = waypoints[waypoints.size - 1]
    val angle = calculateArrowAngle(
        lastSegmentStart.x,
        lastSegmentStart.y,
        lastSegmentEnd.x,
        lastSegmentEnd.y
    )
    drawArrowhead(lastSegmentEnd.x, lastSegmentEnd.y, angle, color)

    // Draw label if there are metabolites to show
    if (edgePath.metabolites.isNotEmpty()) {
        val labelPosition = calculateLabelPosition(waypoints)
        drawEdgeLabel(
            position = labelPosition,
            metabolites = edgePath.metabolites,
            color = color,
            textMeasurer = textMeasurer,
            colorScheme = colorScheme
        )
    }
}

/**
 * Draws an arrowhead at the specified position and angle.
 */
fun DrawScope.drawArrowhead(
    x: Float,
    y: Float,
    angle: Float,
    color: Color
) {
    val arrowSize = 10f
    val arrowAngle = Math.toRadians(30.0).toFloat() // 30 degrees for arrow spread

    // Calculate the three points of the triangle
    val p1 = Offset(x, y) // Tip of the arrow

    val p2 = Offset(
        x - arrowSize * cos(angle - arrowAngle),
        y - arrowSize * sin(angle - arrowAngle)
    )

    val p3 = Offset(
        x - arrowSize * cos(angle + arrowAngle),
        y - arrowSize * sin(angle + arrowAngle)
    )

    // Draw the triangle
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }

    drawPath(
        path = path,
        color = color
    )
}

/**
 * Draws a label showing metabolite quantities on an edge.
 */
fun DrawScope.drawEdgeLabel(
    position: Offset,
    metabolites: List<jon.sharp.metabolism.simulator.model.body.MetaboliteTransfer>,
    color: Color,
    textMeasurer: TextMeasurer,
    colorScheme: ColorScheme
) {
    // Filter to only show metabolites with actual transport (threshold: 0.001 mmol)
    val activeMetabolites = metabolites.filter { it.actualAmount > 0.001f }
    if (activeMetabolites.isEmpty()) return

    // Build label text
    val labelText = if (activeMetabolites.size <= 2) {
        // Show details for 1-2 metabolites with amounts
        activeMetabolites.joinToString("\n") {
            "${it.type}: ${formatWeight(it.actualAmount)}"
        }
    } else {
        // Show only names for 3+ metabolites to avoid clutter
        activeMetabolites.joinToString("\n") {
            it.type.toString()
        }
    }

    val textStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        color = colorScheme.onSurface
    )

    val textResult = textMeasurer.measure(labelText, textStyle)

    // Background box with padding
    val padding = 4f
    val boxWidth = textResult.size.width + (padding * 2)
    val boxHeight = textResult.size.height + (padding * 2)

    val boxTopLeft = Offset(
        x = position.x - (boxWidth / 2),
        y = position.y - boxHeight - 10f  // Offset above the line
    )

    // Draw semi-transparent background (darker than edge color for contrast)
    drawRoundRect(
        color = color.copy(alpha = 0.85f),
        topLeft = boxTopLeft,
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Draw border for better definition
    drawRoundRect(
        color = color,
        topLeft = boxTopLeft,
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1f)
    )

    // Draw text
    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(
            x = boxTopLeft.x + padding,
            y = boxTopLeft.y + padding
        )
    )
}
