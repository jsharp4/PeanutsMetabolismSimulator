package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import jon.sharp.metabolism.simulator.model.body.OrganLayoutConfig
import kotlin.math.max
import kotlin.math.atan2

/**
 * Represents the four sides of a node for edge connections.
 */
enum class NodeSide {
    TOP, BOTTOM, LEFT, RIGHT
}

/**
 * Calculates the connection point on a specific side of a node.
 */
fun getConnectionPoint(position: NodePosition, side: NodeSide, offset: Float = 0f): Offset {
    return when (side) {
        NodeSide.TOP -> Offset(
            x = position.x + (position.width / 2) + offset,
            y = position.y
        )
        NodeSide.BOTTOM -> Offset(
            x = position.x + (position.width / 2) + offset,
            y = position.y + position.height
        )
        NodeSide.LEFT -> Offset(
            x = position.x,
            y = position.y + (position.height / 2)
        )
        NodeSide.RIGHT -> Offset(
            x = position.x + position.width,
            y = position.y + (position.height / 2)
        )
    }
}

/**
 * Determines which side of the destination node to connect to based on simple row/column rules.
 * Rules:
 * - If origin row < destination row: connect to TOP of destination
 * - If origin row > destination row: connect to LEFT (if origin column > dest column) or RIGHT (if origin column < dest column)
 * - If origin row == destination row: connect to LEFT or RIGHT based on column comparison
 */
fun determineDestinationSide(
    fromOrganName: String,
    toOrganName: String,
    fromRow: Int,
    toRow: Int
): NodeSide {
    val fromColumn = OrganLayoutConfig.getColumnNumber(fromOrganName)
    val toColumn = OrganLayoutConfig.getColumnNumber(toOrganName)

    return when {
        fromRow < toRow -> NodeSide.TOP
        fromRow > toRow || fromRow == toRow -> {
            if (fromColumn > toColumn) NodeSide.LEFT else NodeSide.RIGHT
        }
        else -> NodeSide.TOP // fallback
    }
}

/**
 * Checks if a line segment from (x1, y1) to (x2, y2) intersects with a node's bounding box.
 * Uses a margin around the node to ensure clearance.
 */
fun lineSegmentIntersectsNode(
    x1: Float, y1: Float, x2: Float, y2: Float,
    nodePosition: NodePosition,
    margin: Float = 10f
): Boolean {
    val nodeLeft = nodePosition.x - margin
    val nodeRight = nodePosition.x + nodePosition.width + margin
    val nodeTop = nodePosition.y - margin
    val nodeBottom = nodePosition.y + nodePosition.height + margin

    // Check if the line segment intersects the expanded node rectangle
    // Using Liang-Barsky algorithm for line-rectangle intersection

    val dx = x2 - x1
    val dy = y2 - y1

    if (dx == 0f && dy == 0f) {
        // Point check
        return x1 >= nodeLeft && x1 <= nodeRight && y1 >= nodeTop && y1 <= nodeBottom
    }

    var t0 = 0f
    var t1 = 1f

    // Check against each edge
    val edges = listOf(
        Pair(-dx, x1 - nodeLeft),   // left
        Pair(dx, nodeRight - x1),    // right
        Pair(-dy, y1 - nodeTop),     // top
        Pair(dy, nodeBottom - y1)    // bottom
    )

    for ((p, q) in edges) {
        if (p == 0f) {
            if (q < 0f) return false
        } else {
            val r = q / p
            if (p < 0f) {
                if (r > t1) return false
                if (r > t0) t0 = r
            } else {
                if (r < t0) return false
                if (r < t1) t1 = r
            }
        }
    }

    return t0 <= t1
}

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
                    edgeInfo.fromNode,
                    edgeInfo.toNode,
                    edgeInfo.fromPosition,
                    edgeInfo.toPosition,
                    edgeInfo.fromLayer,
                    edgeInfo.toLayer,
                    edgeInfo.isBackEdge,
                    offset,
                    nodePositions
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
     * Calculates orthogonal waypoints (right-angle routing) between two nodes.
     * All edges start from the BOTTOM of the source node.
     * Destination side is determined by row/column rules.
     * Includes obstacle avoidance to ensure edges don't pass through nodes.
     * @param offset Horizontal offset for parallel edge routing
     * @param nodePositions Map of all node positions for obstacle checking
     */
    private fun calculateOrthogonalWaypoints(
        fromOrganName: String,
        toOrganName: String,
        fromPosition: NodePosition,
        toPosition: NodePosition,
        fromRow: Int,
        toRow: Int,
        isBackEdge: Boolean,
        offset: Float = 0f,
        nodePositions: Map<String, NodePosition>
    ): List<Offset> {
        val waypoints = mutableListOf<Offset>()

        // Always start from BOTTOM of source node
        val startPoint = getConnectionPoint(fromPosition, NodeSide.BOTTOM, offset)
        waypoints.add(startPoint)

        // Determine destination side based on row/column rules
        val destinationSide = determineDestinationSide(fromOrganName, toOrganName, fromRow, toRow)
        val endPoint = getConnectionPoint(toPosition, destinationSide, offset)

        if (isBackEdge) {
            // For back edges, route around to the side to avoid overlapping nodes
            val routeOffset = 60f + kotlin.math.abs(offset)
            val sideX = max(fromPosition.x + fromPosition.width, toPosition.x + toPosition.width) + routeOffset

            // Go down, then to the side, then to destination
            waypoints.add(Offset(startPoint.x, startPoint.y + 30f))
            waypoints.add(Offset(sideX, startPoint.y + 30f))
            waypoints.add(Offset(sideX, endPoint.y))
            waypoints.add(endPoint)
        } else {
            // For forward edges, create orthogonal path based on destination side
            when (destinationSide) {
                NodeSide.TOP -> {
                    // Destination is below source - vertical routing
                    if (kotlin.math.abs(endPoint.x - startPoint.x) < 10f) {
                        // Nearly vertical - direct line
                        waypoints.add(endPoint)
                    } else {
                        // Use midpoint for right-angle routing
                        val midY = (startPoint.y + endPoint.y) / 2
                        waypoints.add(Offset(startPoint.x, midY))
                        waypoints.add(Offset(endPoint.x, midY))
                        waypoints.add(endPoint)
                    }
                }
                NodeSide.LEFT, NodeSide.RIGHT -> {
                    // Destination is to the side - route down, across, then to side
                    val verticalClearance = 30f
                    waypoints.add(Offset(startPoint.x, startPoint.y + verticalClearance))
                    waypoints.add(Offset(endPoint.x, startPoint.y + verticalClearance))
                    waypoints.add(Offset(endPoint.x, endPoint.y))
                    waypoints.add(endPoint)
                }
                NodeSide.BOTTOM -> {
                    // Should not happen with our rules, but handle it
                    waypoints.add(endPoint)
                }
            }
        }

        // Check for obstacles and adjust waypoints if needed
        return avoidObstacles(waypoints, fromOrganName, toOrganName, nodePositions)
    }

    /**
     * Checks if the path intersects any nodes and adds waypoints to route around them.
     */
    private fun avoidObstacles(
        originalWaypoints: List<Offset>,
        fromOrganName: String,
        toOrganName: String,
        nodePositions: Map<String, NodePosition>
    ): List<Offset> {
        val result = mutableListOf<Offset>()
        result.add(originalWaypoints.first())

        for (i in 0 until originalWaypoints.size - 1) {
            val start = originalWaypoints[i]
            val end = originalWaypoints[i + 1]

            // Check if this segment intersects any node (except source and destination)
            val intersectingNodes = nodePositions.filter { (organName, nodePos) ->
                organName != fromOrganName &&
                organName != toOrganName &&
                lineSegmentIntersectsNode(start.x, start.y, end.x, end.y, nodePos)
            }

            if (intersectingNodes.isEmpty()) {
                // No obstacles, add the endpoint
                result.add(end)
            } else {
                // There's an obstacle, route around it
                val obstacle = intersectingNodes.values.first()

                // Determine whether to route around left or right
                // Choose based on which side is shorter
                val obstacleLeft = obstacle.x - 20f
                val obstacleRight = obstacle.x + obstacle.width + 20f
                val obstacleTop = obstacle.y - 20f
                val obstacleBottom = obstacle.y + obstacle.height + 20f

                // For horizontal segments, route around top or bottom
                if (kotlin.math.abs(start.y - end.y) < 5f) {
                    // Horizontal segment - route around top or bottom
                    val routeTop = listOf(
                        Offset(start.x, obstacleTop),
                        Offset(end.x, obstacleTop)
                    )
                    val routeBottom = listOf(
                        Offset(start.x, obstacleBottom),
                        Offset(end.x, obstacleBottom)
                    )

                    // Check which route is shorter and doesn't intersect other obstacles
                    val useTop = kotlin.math.abs(obstacleTop - start.y) < kotlin.math.abs(obstacleBottom - start.y)
                    val detourWaypoints = if (useTop) routeTop else routeBottom

                    result.addAll(detourWaypoints)
                } else {
                    // Vertical segment - route around left or right
                    val routeLeft = listOf(
                        Offset(obstacleLeft, start.y),
                        Offset(obstacleLeft, end.y)
                    )
                    val routeRight = listOf(
                        Offset(obstacleRight, start.y),
                        Offset(obstacleRight, end.y)
                    )

                    // Check which route is shorter
                    val useLeft = kotlin.math.abs(obstacleLeft - start.x) < kotlin.math.abs(obstacleRight - start.x)
                    val detourWaypoints = if (useLeft) routeLeft else routeRight

                    result.addAll(detourWaypoints)
                }

                result.add(end)
            }
        }

        return result
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
