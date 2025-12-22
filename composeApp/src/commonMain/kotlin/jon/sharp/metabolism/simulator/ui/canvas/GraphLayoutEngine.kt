package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import jon.sharp.metabolism.simulator.model.body.OrganLayoutConfig
import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Represents the four sides of a node for edge connections.
 */
enum class NodeSide {
    TOP, BOTTOM, LEFT, RIGHT
}

fun getConnectionPoint(position: NodePosition, side: NodeSide, offset: Float = 0f): Offset {
    return when (side) {
        NodeSide.TOP -> Offset(position.x + (position.width / 2) + offset, position.y)
        NodeSide.BOTTOM -> Offset(position.x + (position.width / 2) + offset, position.y + position.height)
        NodeSide.LEFT -> Offset(position.x, position.y + (position.height / 2))
        NodeSide.RIGHT -> Offset(position.x + position.width, position.y + (position.height / 2))
    }
}

/**
 * Determines which side to connect to.
 * Forward flow: Connect to TOP.
 * Same-row or Back-flow: Connect to SIDE (LEFT/RIGHT).
 */
fun determineDestinationSide(fromOrganName: String, toOrganName: String, fromRow: Int, toRow: Int): NodeSide {
    val fromCol = OrganLayoutConfig.getColumnNumber(fromOrganName)
    val toCol = OrganLayoutConfig.getColumnNumber(toOrganName)

    return when {
        fromRow < toRow -> NodeSide.TOP
        else -> {
            // Same row or back edge: wrap around to the side nearest the source
            if (fromCol > toCol) NodeSide.RIGHT else NodeSide.LEFT
        }
    }
}

fun lineSegmentIntersectsNode(x1: Float, y1: Float, x2: Float, y2: Float, nodePos: NodePosition, margin: Float = 10f): Boolean {
    val nodeLeft = nodePos.x - margin
    val nodeRight = nodePos.x + nodePos.width + margin
    val nodeTop = nodePos.y - margin
    val nodeBottom = nodePos.y + nodePos.height + margin

    val dx = x2 - x1
    val dy = y2 - y1
    if (dx == 0f && dy == 0f) return x1 >= nodeLeft && x1 <= nodeRight && y1 >= nodeTop && y1 <= nodeBottom

    var t0 = 0f
    var t1 = 1f
    val edges = listOf(Pair(-dx, x1 - nodeLeft), Pair(dx, nodeRight - x1), Pair(-dy, y1 - nodeTop), Pair(dy, nodeBottom - y1))

    for ((p, q) in edges) {
        if (p == 0f) { if (q < 0f) return false }
        else {
            val r = q / p
            if (p < 0f) { if (r > t1) return false; if (r > t0) t0 = r }
            else { if (r < t0) return false; if (r < t1) t1 = r }
        }
    }
    return t0 <= t1
}

class GraphLayoutEngine(private val nodes: List<GraphNodeWithMetabolites>, private val availableWidth: Float) {
    companion object {
        const val NODE_WIDTH = 260f
        const val NODE_MIN_HEIGHT = 84f
        const val NODE_PADDING = 12f
        const val LINE_HEIGHT = 20f
        const val HORIZONTAL_NODE_SPACING = 60f
        const val LAYER_SPACING = 90f
        const val CANVAS_PADDING = 50f

        private val COLOR_PALETTE = listOf(
            Color(0xFF6B9BD1), Color(0xFF8CC152), Color(0xFFFFCE54),
            Color(0xFFFC6E51), Color(0xFFAC92EC), Color(0xFF4FC1E9)
        )
        private fun getNodeColor(index: Int): Color = COLOR_PALETTE[index % COLOR_PALETTE.size]
    }

    fun computeLayout(): LayoutResult {
        if (nodes.isEmpty()) return LayoutResult(emptyMap(), emptyList(), 0f, 0f)
        val layerMap = nodes.associate { it.organName to it.rowNumber }
        val (nodePositions, nodeColorMap) = calculateVerticalNodePositions(layerMap)
        val edgePaths = calculateEdgePaths(nodePositions, layerMap, nodeColorMap)
        val bounds = calculateCanvasBounds(nodePositions)
        return LayoutResult(nodePositions, edgePaths, bounds.width, bounds.height)
    }

    private fun calculateVerticalNodePositions(layerMap: Map<String, Int>): Pair<Map<String, NodePosition>, Map<String, Color>> {
        val positions = mutableMapOf<String, NodePosition>()
        val colorMap = mutableMapOf<String, Color>()
        val layers = nodes.groupBy { layerMap[it.organName] ?: 0 }.toSortedMap()

        val maxCols = layers.values.maxOfOrNull { it.size } ?: 1
        val totalWidth = (maxCols * NODE_WIDTH) + ((maxCols - 1) * HORIZONTAL_NODE_SPACING)
        val startX = (availableWidth - totalWidth) / 2

        var currentY = CANVAS_PADDING
        layers.forEach { (_, nodesInLayer) ->
            nodesInLayer.sortedBy { OrganLayoutConfig.getColumnNumber(it.organName) }.forEachIndexed { idx, node ->
                val x = startX + (idx * (NODE_WIDTH + HORIZONTAL_NODE_SPACING))
                val color = getNodeColor(positions.size)
                positions[node.organName] = NodePosition(node.organName, x, currentY, NODE_WIDTH, NODE_MIN_HEIGHT, color)
                colorMap[node.organName] = color
            }
            currentY += NODE_MIN_HEIGHT + LAYER_SPACING
        }
        return Pair(positions, colorMap)
    }

    private fun calculateEdgePaths(nodePositions: Map<String, NodePosition>, layerMap: Map<String, Int>, nodeColorMap: Map<String, Color>): List<EdgePath> {
        val edgePaths = mutableListOf<EdgePath>()
        nodes.forEach { sourceNode ->
            sourceNode.edges.forEach { edge ->
                val fromPos = nodePositions[sourceNode.organName] ?: return@forEach
                val toPos = nodePositions[edge.destinationName] ?: return@forEach
                val fromLayer = layerMap[sourceNode.organName] ?: 0
                val toLayer = layerMap[edge.destinationName] ?: 0

                val siblingEdges = sourceNode.edges.filter { it.destinationName == edge.destinationName }
                val edgeIndex = siblingEdges.indexOf(edge)
                // Offset parallel edges slightly
                val offset = if (siblingEdges.size > 1) (edgeIndex - (siblingEdges.size - 1) / 2f) * 20f else 0f

                val waypoints = calculateOrthogonalWaypoints(
                    sourceNode.organName, edge.destinationName, fromPos, toPos,
                    fromLayer, toLayer, offset, nodePositions
                )
                edgePaths.add(EdgePath(sourceNode.organName, edge.destinationName, toLayer <= fromLayer, waypoints, nodeColorMap[sourceNode.organName] ?: Color.Gray, edge.metabolites))
            }
        }
        return edgePaths
    }

    private fun calculateOrthogonalWaypoints(
        fromName: String, toName: String, fromPos: NodePosition, toPos: NodePosition,
        fromRow: Int, toRow: Int, offset: Float, nodePositions: Map<String, NodePosition>
    ): List<Offset> {
        // Always exit from the BOTTOM
        val start = getConnectionPoint(fromPos, NodeSide.BOTTOM, offset)
        val side = determineDestinationSide(fromName, toName, fromRow, toRow)
        val end = getConnectionPoint(toPos, side, offset)

        val waypoints = mutableListOf(start)

        // 1. Calculate the "Safe Corridors" (Gutters)
        // Gutter below the source node
        val sourceGutterY = fromPos.y + fromPos.height + (LAYER_SPACING / 2f)
        // Gutter above the target node
        val targetGutterY = toPos.y - (LAYER_SPACING / 2f)

        if (fromRow == toRow) {
            // SAME ROW ROUTING: Drop to gutter, move across, move back up to side
            val sideX = if (start.x < end.x) toPos.x - 30f else toPos.x + toPos.width + 30f
            waypoints.add(Offset(start.x, sourceGutterY))
            waypoints.add(Offset(sideX, sourceGutterY))
            waypoints.add(Offset(sideX, end.y))
        } else if (fromRow < toRow) {
            // FORWARD ROUTING: Drop to gutter, move to target X, drop to target
            // Check if there is a vertical obstacle directly between them
            val straightPathBlocked = isBlocked(start, Offset(start.x, toPos.y), fromName, toName, nodePositions)

            if (abs(start.x - end.x) < 5f && !straightPathBlocked) {
                // Perfect vertical alignment, no obstacle: straight line
            } else {
                // Must move horizontally. Use the gutter between rows.
                waypoints.add(Offset(start.x, sourceGutterY))
                waypoints.add(Offset(end.x, sourceGutterY))
            }
        } else {
            // BACK-EDGE ROUTING: Wrap around to the far side
            val farSideX = max(fromPos.x + fromPos.width, toPos.x + toPos.width) + 60f
            waypoints.add(Offset(start.x, sourceGutterY))
            waypoints.add(Offset(farSideX, sourceGutterY))
            waypoints.add(Offset(farSideX, targetGutterY))
            waypoints.add(Offset(end.x - 30f, targetGutterY)) // Approach from left side
            waypoints.add(Offset(end.x - 30f, end.y))
        }

        waypoints.add(end)
        return waypoints
    }

    private fun isBlocked(p1: Offset, p2: Offset, ignore1: String, ignore2: String, nodes: Map<String, NodePosition>): Boolean {
        return nodes.any { (name, pos) ->
            name != ignore1 && name != ignore2 && lineSegmentIntersectsNode(p1.x, p1.y, p2.x, p2.y, pos, margin = 10f)
        }
    }

    private fun calculateCanvasBounds(nodePositions: Map<String, NodePosition>): Size {
        val maxX = nodePositions.values.maxOfOrNull { it.x + it.width } ?: 0f
        val maxY = nodePositions.values.maxOfOrNull { it.y + it.height } ?: 0f
        return Size(maxX + CANVAS_PADDING, maxY + CANVAS_PADDING)
    }
}

fun calculateLabelPosition(
    waypoints: List<Offset>,
    nodePositions: Map<String, NodePosition>
): Offset {
    if (waypoints.size < 2) return waypoints.firstOrNull() ?: Offset.Zero

    val horizontalSegments = mutableListOf<Pair<Offset, Offset>>()
    for (i in 0 until waypoints.size - 1) {
        if (abs(waypoints[i].y - waypoints[i+1].y) < 2f) {
            horizontalSegments.add(waypoints[i] to waypoints[i+1])
        }
    }

    val sortedSegments = horizontalSegments.sortedByDescending { abs(it.first.x - it.second.x) }

    for (seg in sortedSegments) {
        val midX = (seg.first.x + seg.second.x) / 2
        val y = seg.first.y

        val overlaps = nodePositions.values.any { node ->
            val margin = 15f
            val nL = node.x - margin
            val nR = node.x + node.width + margin
            val nT = node.y - margin
            val nB = node.y + node.height + margin
            midX in nL..nR && y in nT..nB
        }

        if (!overlaps) return Offset(midX, y)
    }

    val longest = sortedSegments.firstOrNull() ?: return waypoints[waypoints.size / 2]
    return Offset((longest.first.x + longest.second.x) / 2, longest.first.y)
}

fun calculateArrowAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float = atan2(y2 - y1, x2 - x1)