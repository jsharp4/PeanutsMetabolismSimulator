package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import jon.sharp.metabolism.simulator.model.body.OrganLayoutConfig
import kotlin.math.max
import kotlin.math.atan2

/**
 * Layout engine that computes spatial positions for graph nodes using an adaptive hierarchical layout.
 */
class GraphLayoutEngine(
    private val nodes: List<GraphNodeWithMetabolites>,
    private val availableWidth: Float
) {
    companion object {
        const val NODE_WIDTH = 320f // Increased to accommodate organ images (60px + padding + text)
        const val NODE_MIN_HEIGHT = 100f
        const val NODE_PADDING = 12f
        const val HORIZONTAL_NODE_SPACING = 40f // Horizontal gap between nodes in same layer
        const val LAYER_SPACING = 120f // Vertical gap between layers
        const val LINE_HEIGHT = 20f
        const val CANVAS_PADDING = 40f

        // Color palette for nodes
        private val COLOR_PALETTE = listOf(
            Color(0xFF6B9BD1), // Blue
            Color(0xFF8CC152), // Green
            Color(0xFFFFCE54), // Yellow
            Color(0xFFFC6E51), // Red/Orange
            Color(0xFFAC92EC), // Purple
            Color(0xFF4FC1E9), // Cyan
            Color(0xFFED5565), // Pink
            Color(0xFFA0D468), // Light Green
            Color(0xFFFFCD00), // Gold
            Color(0xFF48CFAD)  // Teal
        )

        private fun getNodeColor(index: Int): Color {
            return COLOR_PALETTE[index % COLOR_PALETTE.size]
        }
    }

    /**
     * Computes the complete layout including node positions and edge paths.
     */
    fun computeLayout(): LayoutResult {
        if (nodes.isEmpty()) {
            return LayoutResult(
                nodePositions = emptyMap(),
                edgePaths = emptyList(),
                canvasWidth = 0f,
                canvasHeight = 0f
            )
        }

        val layerMap = assignLayers()
        val (nodePositions, nodeColorMap) = calculateVerticalNodePositions(layerMap)
        val edgePaths = calculateEdgePaths(nodePositions, layerMap, nodeColorMap)
        val bounds = calculateCanvasBounds(nodePositions)

        return LayoutResult(
            nodePositions = nodePositions,
            edgePaths = edgePaths,
            canvasWidth = bounds.width,
            canvasHeight = bounds.height
        )
    }

    /**
     * Assigns layers to nodes using the rowNumber from each GraphNode.
     * Returns a map of organ name to layer (row) number.
     */
    private fun assignLayers(): Map<String, Int> {
        return nodes.associate { node ->
            node.organName to node.rowNumber
        }
    }

    /**
     * Calculates node positions using vertical top-to-bottom layout.
     * Uses OrganLayoutConfig column numbers for horizontal positioning.
     * Returns both the positions and a color map for nodes.
     */
    private fun calculateVerticalNodePositions(
        layerMap: Map<String, Int>
    ): Pair<Map<String, NodePosition>, Map<String, Color>> {
        val positions = mutableMapOf<String, NodePosition>()
        val colorMap = mutableMapOf<String, Color>()

        // Group nodes by layer (row)
        val layers = mutableMapOf<Int, MutableList<GraphNodeWithMetabolites>>()
        nodes.forEach { node ->
            val layer = layerMap[node.organName] ?: 0
            layers.getOrPut(layer) { mutableListOf() }.add(node)
        }

        // Sort nodes within each layer by their column number
        layers.values.forEach { nodesInLayer ->
            nodesInLayer.sortBy { OrganLayoutConfig.getColumnNumber(it.organName) }
        }

        // Find max columns across all layers for consistent width calculation
        val maxColumns = layers.values.maxOfOrNull { nodesInLayer ->
            nodesInLayer.maxOfOrNull { OrganLayoutConfig.getColumnNumber(it.organName) } ?: 0
        } ?: 0

        // Calculate total width needed for max columns
        val totalWidth = ((maxColumns + 1) * NODE_WIDTH) + (maxColumns * HORIZONTAL_NODE_SPACING)
        val startX = (availableWidth - totalWidth) / 2

        // Calculate Y position for each layer
        var currentY = CANVAS_PADDING
        var colorIndex = 0

        layers.keys.sorted().forEach { layer ->
            val nodesInLayer = layers[layer] ?: emptyList()

            // Find the max height in this layer for consistent spacing
            val maxHeightInLayer = nodesInLayer.maxOfOrNull { calculateNodeHeight(it) } ?: NODE_MIN_HEIGHT

            // Position each node in this layer using its column number
            nodesInLayer.forEach { node ->
                val column = OrganLayoutConfig.getColumnNumber(node.organName)
                val nodeX = startX + (column * (NODE_WIDTH + HORIZONTAL_NODE_SPACING))
                val height = calculateNodeHeight(node)
                val color = getNodeColor(colorIndex++)

                positions[node.organName] = NodePosition(
                    organName = node.organName,
                    x = nodeX,
                    y = currentY,
                    width = NODE_WIDTH,
                    height = height,
                    color = color
                )

                colorMap[node.organName] = color
            }

            // Move to next layer
            currentY += maxHeightInLayer + LAYER_SPACING
        }

        return Pair(positions, colorMap)
    }

    /**
     * Calculates the height of a node based on its metabolite count.
     */
    private fun calculateNodeHeight(node: GraphNodeWithMetabolites): Float {
        val metaboliteLines = max(1, node.metabolites.size)
        return NODE_MIN_HEIGHT + (metaboliteLines * LINE_HEIGHT) + (2 * NODE_PADDING)
    }

    /**
     * Calculates edge paths between positioned nodes using orthogonal routing.
     * Handles parallel routing for edges that would otherwise overlap.
     */
    private fun calculateEdgePaths(
        nodePositions: Map<String, NodePosition>,
        layerMap: Map<String, Int>,
        nodeColorMap: Map<String, Color>
    ): List<EdgePath> {
        // First, collect all edges and group them by source-destination pair
        data class EdgeInfo(
            val fromNode: String,
            val toNode: String,
            val fromPosition: NodePosition,
            val toPosition: NodePosition,
            val fromLayer: Int,
            val toLayer: Int,
            val isBackEdge: Boolean,
            val color: Color
        )

        val allEdges = mutableListOf<EdgeInfo>()

        nodes.forEach { node ->
            val fromPosition = nodePositions[node.organName] ?: return@forEach
            val fromLayer = layerMap[node.organName] ?: 0
            val nodeColor = nodeColorMap[node.organName] ?: Color.Gray

            node.edges.forEach { edge ->
                val toPosition = nodePositions[edge.destinationName] ?: return@forEach
                val toLayer = layerMap[edge.destinationName] ?: 0
                val isBackEdge = toLayer <= fromLayer

                allEdges.add(EdgeInfo(
                    fromNode = node.organName,
                    toNode = edge.destinationName,
                    fromPosition = fromPosition,
                    toPosition = toPosition,
                    fromLayer = fromLayer,
                    toLayer = toLayer,
                    isBackEdge = isBackEdge,
                    color = nodeColor
                ))
            }
        }

        // Group edges by destination node (edges converging to same node need parallel routing)
        val edgeGroups = allEdges.groupBy { edge ->
            edge.toNode
        }

        val edgePaths = mutableListOf<EdgePath>()

        edgeGroups.forEach { (_, group) ->
            val groupSize = group.size
            val parallelOffset = 15f // Horizontal offset for parallel edges

            group.forEachIndexed { index, edgeInfo ->
                // Calculate offset from center for this edge in the group
                val offset = if (groupSize > 1) {
                    (index - (groupSize - 1) / 2.0f) * parallelOffset
                } else {
                    0f
                }

                // Calculate orthogonal waypoints with parallel offset
                val waypoints = calculateOrthogonalWaypoints(
                    edgeInfo.fromPosition,
                    edgeInfo.toPosition,
                    edgeInfo.isBackEdge,
                    offset
                )

                // Extract metabolites from the source node's edges
                val sourceNode = nodes.find { it.organName == edgeInfo.fromNode }
                val metabolites = sourceNode?.edges
                    ?.find { it.destinationName == edgeInfo.toNode }
                    ?.metabolites ?: emptyList()

                edgePaths.add(
                    EdgePath(
                        fromNode = edgeInfo.fromNode,
                        toNode = edgeInfo.toNode,
                        isBackEdge = edgeInfo.isBackEdge,
                        waypoints = waypoints,
                        color = edgeInfo.color,
                        metabolites = metabolites
                    )
                )
            }
        }

        return edgePaths
    }

    /**
     * Calculates orthogonal waypoints (right-angle routing) between two nodes for vertical layout.
     * @param offset Horizontal offset for parallel edge routing
     */
    private fun calculateOrthogonalWaypoints(
        fromPosition: NodePosition,
        toPosition: NodePosition,
        isBackEdge: Boolean,
        offset: Float = 0f
    ): List<Offset> {
        val waypoints = mutableListOf<Offset>()

        // Start from bottom-center of source node (with offset)
        val startX = fromPosition.x + (fromPosition.width / 2) + offset
        val startY = fromPosition.y + fromPosition.height
        waypoints.add(Offset(startX, startY))

        // End at top-center of destination node (with offset)
        val endX = toPosition.x + (toPosition.width / 2) + offset
        val endY = toPosition.y

        if (isBackEdge) {
            // For back edges, route around to the side to avoid overlapping nodes
            val routeOffset = 60f + kotlin.math.abs(offset)
            val sideX = max(fromPosition.x + fromPosition.width, toPosition.x + toPosition.width) + routeOffset

            // Go down, then to the side, then up to destination
            waypoints.add(Offset(startX, startY + 30f))
            waypoints.add(Offset(sideX, startY + 30f))
            waypoints.add(Offset(sideX, endY - 30f))
            waypoints.add(Offset(endX, endY - 30f))
            waypoints.add(Offset(endX, endY))
        } else {
            // For forward edges, use simple vertical routing
            if (kotlin.math.abs(endX - startX) < 10f) {
                // Nearly vertical - direct line (with offset applied)
                waypoints.add(Offset(endX, endY))
            } else {
                // Use midpoint for right-angle routing (offset maintains parallel paths)
                val midY = (startY + endY) / 2
                waypoints.add(Offset(startX, midY))
                waypoints.add(Offset(endX, midY))
                waypoints.add(Offset(endX, endY))
            }
        }

        return waypoints
    }

    /**
     * Calculates the bounding box for the entire canvas.
     */
    private fun calculateCanvasBounds(nodePositions: Map<String, NodePosition>): Size {
        if (nodePositions.isEmpty()) {
            return Size(0f, 0f)
        }

        var maxX = 0f
        var maxY = 0f

        nodePositions.values.forEach { position ->
            maxX = max(maxX, position.x + position.width)
            maxY = max(maxY, position.y + position.height)
        }

        return Size(
            width = maxX + CANVAS_PADDING,
            height = maxY + CANVAS_PADDING
        )
    }
}

/**
 * Calculates optimal position for edge label along the path.
 * Prefers horizontal segments for better readability.
 */
fun calculateLabelPosition(waypoints: List<Offset>): Offset {
    if (waypoints.size < 2) return waypoints.firstOrNull() ?: Offset.Zero

    // For simple vertical paths (2 waypoints), use midpoint
    if (waypoints.size == 2) {
        return Offset(
            x = waypoints[0].x,
            y = (waypoints[0].y + waypoints[1].y) / 2
        )
    }

    // For orthogonal paths, find the first horizontal segment (best for readability)
    for (i in 0 until waypoints.size - 1) {
        val start = waypoints[i]
        val end = waypoints[i + 1]

        // Horizontal segment (y is same, x differs significantly)
        if (kotlin.math.abs(start.y - end.y) < 1f && kotlin.math.abs(start.x - end.x) > 30f) {
            return Offset(
                x = (start.x + end.x) / 2,
                y = start.y
            )
        }
    }

    // Fallback: use middle waypoint
    return waypoints[waypoints.size / 2]
}

/**
 * Calculates the angle in radians for an arrowhead pointing from (x1, y1) to (x2, y2).
 */
fun calculateArrowAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return atan2(y2 - y1, x2 - x1)
}
