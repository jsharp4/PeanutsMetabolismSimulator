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
     * Ensures perpendicular approach and minimum turns while avoiding obstacles.
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
        // Always start from BOTTOM of source node
        val startPoint = getConnectionPoint(fromPosition, NodeSide.BOTTOM, offset)

        // Determine destination side based on row/column rules
        val destinationSide = determineDestinationSide(fromOrganName, toOrganName, fromRow, toRow)
        val endPoint = getConnectionPoint(toPosition, destinationSide, offset)

        // Calculate perpendicular approach point
        val approachDistance = 40f
        val approachPoint = when (destinationSide) {
            NodeSide.TOP -> Offset(endPoint.x, endPoint.y - approachDistance)
            NodeSide.BOTTOM -> Offset(endPoint.x, endPoint.y + approachDistance)
            NodeSide.LEFT -> Offset(endPoint.x - approachDistance, endPoint.y)
            NodeSide.RIGHT -> Offset(endPoint.x + approachDistance, endPoint.y)
        }

        // Build path with obstacle avoidance
        val waypoints = buildObstacleFreePath(
            startPoint,
            approachPoint,
            endPoint,
            destinationSide,
            fromOrganName,
            toOrganName,
            nodePositions,
            fromPosition,
            toPosition,
            isBackEdge
        )

        return waypoints
    }

    /**
     * Builds a path from start to approach point to end, avoiding all obstacles.
     */
    private fun buildObstacleFreePath(
        start: Offset,
        approach: Offset,
        end: Offset,
        endSide: NodeSide,
        fromOrganName: String,
        toOrganName: String,
        nodePositions: Map<String, NodePosition>,
        fromPosition: NodePosition,
        toPosition: NodePosition,
        isBackEdge: Boolean
    ): List<Offset> {
        val waypoints = mutableListOf<Offset>()
        waypoints.add(start)

        if (isBackEdge) {
            // For back edges, route far to the right side
            val margin = 80f
            val sideX = max(fromPosition.x + fromPosition.width, toPosition.x + toPosition.width) + margin
            waypoints.add(Offset(start.x, start.y + 30f))
            waypoints.add(Offset(sideX, start.y + 30f))
            waypoints.add(Offset(sideX, approach.y))
            waypoints.add(approach)
            waypoints.add(end)
            return waypoints
        }

        // For forward edges, route with minimum turns
        when (endSide) {
            NodeSide.TOP -> {
                // Approaching from above - need vertical final approach
                routeForVerticalApproach(waypoints, start, approach, fromOrganName, toOrganName, nodePositions)
            }
            NodeSide.BOTTOM -> {
                // Approaching from below - need vertical final approach
                routeForVerticalApproach(waypoints, start, approach, fromOrganName, toOrganName, nodePositions)
            }
            NodeSide.LEFT, NodeSide.RIGHT -> {
                // Approaching from side - need horizontal final approach
                routeForHorizontalApproach(waypoints, start, approach, fromOrganName, toOrganName, nodePositions)
            }
        }

        waypoints.add(end)
        return waypoints
    }

    /**
     * Routes to a point that needs vertical final approach.
     */
    private fun routeForVerticalApproach(
        waypoints: MutableList<Offset>,
        start: Offset,
        approach: Offset,
        fromOrganName: String,
        toOrganName: String,
        nodePositions: Map<String, NodePosition>
    ) {
        // For vertical approach, we need to get to the X coordinate, then approach vertically
        if (kotlin.math.abs(approach.x - start.x) < 5f) {
            // Already aligned vertically - just check for obstacles
            if (!pathHasObstacles(start, approach, fromOrganName, toOrganName, nodePositions)) {
                waypoints.add(approach)
                return
            }
        }

        // Need to route: down, across, then to approach point
        // Find a safe horizontal corridor
        val corridorY = findSafeHorizontalCorridor(
            start.x, approach.x, start.y, approach.y,
            fromOrganName, toOrganName, nodePositions
        )

        waypoints.add(Offset(start.x, corridorY))
        waypoints.add(Offset(approach.x, corridorY))
        waypoints.add(approach)
    }

    /**
     * Routes to a point that needs horizontal final approach.
     */
    private fun routeForHorizontalApproach(
        waypoints: MutableList<Offset>,
        start: Offset,
        approach: Offset,
        fromOrganName: String,
        toOrganName: String,
        nodePositions: Map<String, NodePosition>
    ) {
        // For horizontal approach, we need: vertical down, horizontal across, vertical to approach Y, then horizontal to approach
        // Find a safe horizontal corridor
        val corridorY = findSafeHorizontalCorridor(
            start.x, approach.x, start.y, approach.y,
            fromOrganName, toOrganName, nodePositions
        )

        waypoints.add(Offset(start.x, corridorY))
        waypoints.add(Offset(approach.x, corridorY))
        waypoints.add(approach)
    }

    /**
     * Finds a horizontal Y coordinate that doesn't intersect any nodes.
     */
    private fun findSafeHorizontalCorridor(
        x1: Float, x2: Float, y1: Float, y2: Float,
        fromOrganName: String,
        toOrganName: String,
        nodePositions: Map<String, NodePosition>
    ): Float {
        val minX = kotlin.math.min(x1, x2)
        val maxX = kotlin.math.max(x1, x2)
        val startY = y1 + 30f
        val endY = y2
        val margin = 30f

        // Try corridors at various Y positions
        val attempts = mutableListOf<Float>()
        attempts.add(startY)

        // Add more potential corridors
        for (i in 1..5) {
            attempts.add(startY + i * 40f)
        }

        // If going upward, try positions above
        if (endY < startY) {
            for (i in 1..3) {
                attempts.add(startY - i * 40f)
            }
        }

        // Test each corridor
        for (testY in attempts) {
            var hasObstacle = false

            for ((organName, nodePos) in nodePositions) {
                if (organName == fromOrganName || organName == toOrganName) continue

                val nodeTop = nodePos.y - margin
                val nodeBottom = nodePos.y + nodePos.height + margin
                val nodeLeft = nodePos.x - margin
                val nodeRight = nodePos.x + nodePos.width + margin

                // Check if corridor at testY would intersect this node
                if (testY >= nodeTop && testY <= nodeBottom) {
                    // Corridor is at the height of this node - check if it overlaps horizontally
                    if (maxX >= nodeLeft && minX <= nodeRight) {
                        hasObstacle = true
                        break
                    }
                }
            }

            if (!hasObstacle) {
                return testY
            }
        }

        // Fallback: return a position far below
        return startY + 100f
    }

    /**
     * Checks if a path between two points intersects any nodes.
     */
    private fun pathHasObstacles(
        from: Offset,
        to: Offset,
        fromOrganName: String,
        toOrganName: String,
        nodePositions: Map<String, NodePosition>
    ): Boolean {
        for ((organName, nodePos) in nodePositions) {
            if (organName == fromOrganName || organName == toOrganName) continue
            if (lineSegmentIntersectsNode(from.x, from.y, to.x, to.y, nodePos, margin = 20f)) {
                return true
            }
        }
        return false
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
