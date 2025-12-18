package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
        const val NODE_WIDTH = 240f
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
     * Assigns layers to nodes using BFS and detects cycles.
     * Returns a map of organ name to layer number.
     */
    private fun assignLayers(): Map<String, Int> {
        val layerMap = mutableMapOf<String, Int>()
        val visited = mutableSetOf<String>()

        // Start from the first node (should be Mouth in the metabolism graph)
        val startNode = nodes.first()
        val queue = ArrayDeque<String>()

        queue.add(startNode.organName)
        layerMap[startNode.organName] = 0
        visited.add(startNode.organName)

        while (queue.isNotEmpty()) {
            val currentName = queue.removeFirst()
            val currentNode = nodes.find { it.organName == currentName }
                ?: continue

            val currentLayer = layerMap[currentName]!!

            currentNode.edges.forEach { edge ->
                val destName = edge.destinationName

                if (destName !in visited) {
                    // Forward edge - assign next layer
                    layerMap[destName] = currentLayer + 1
                    visited.add(destName)
                    queue.add(destName)
                } else {
                    // Back edge detected - node already visited
                    // We'll mark this during edge path calculation
                }
            }
        }

        // Handle any disconnected nodes (shouldn't happen in this graph)
        nodes.forEach { node ->
            if (node.organName !in layerMap) {
                layerMap[node.organName] = 0
            }
        }

        return layerMap
    }

    /**
     * Calculates node positions using vertical top-to-bottom layout.
     * Returns both the positions and a color map for nodes.
     */
    private fun calculateVerticalNodePositions(
        layerMap: Map<String, Int>
    ): Pair<Map<String, NodePosition>, Map<String, Color>> {
        val positions = mutableMapOf<String, NodePosition>()
        val colorMap = mutableMapOf<String, Color>()

        // Group nodes by layer
        val layers = mutableMapOf<Int, MutableList<GraphNodeWithMetabolites>>()
        nodes.forEach { node ->
            val layer = layerMap[node.organName] ?: 0
            layers.getOrPut(layer) { mutableListOf() }.add(node)
        }

        // Calculate Y position for each layer
        var currentY = CANVAS_PADDING
        var colorIndex = 0

        layers.keys.sorted().forEach { layer ->
            val nodesInLayer = layers[layer] ?: emptyList()

            // Calculate total width needed for this layer
            val totalWidth = (nodesInLayer.size * NODE_WIDTH) + ((nodesInLayer.size - 1) * HORIZONTAL_NODE_SPACING)

            // Center the layer horizontally
            var currentX = (availableWidth - totalWidth) / 2

            // Find the max height in this layer for consistent spacing
            val maxHeightInLayer = nodesInLayer.maxOfOrNull { calculateNodeHeight(it) } ?: NODE_MIN_HEIGHT

            // Position each node in this layer
            nodesInLayer.forEach { node ->
                val height = calculateNodeHeight(node)
                val color = getNodeColor(colorIndex++)

                positions[node.organName] = NodePosition(
                    organName = node.organName,
                    x = currentX,
                    y = currentY,
                    width = NODE_WIDTH,
                    height = height,
                    color = color
                )

                colorMap[node.organName] = color
                currentX += NODE_WIDTH + HORIZONTAL_NODE_SPACING
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
     */
    private fun calculateEdgePaths(
        nodePositions: Map<String, NodePosition>,
        layerMap: Map<String, Int>,
        nodeColorMap: Map<String, Color>
    ): List<EdgePath> {
        val edgePaths = mutableListOf<EdgePath>()

        nodes.forEach { node ->
            val fromPosition = nodePositions[node.organName] ?: return@forEach
            val fromLayer = layerMap[node.organName] ?: 0
            val nodeColor = nodeColorMap[node.organName] ?: Color.Gray

            node.edges.forEach { edge ->
                val toPosition = nodePositions[edge.destinationName] ?: return@forEach
                val toLayer = layerMap[edge.destinationName] ?: 0

                // Determine if this is a back edge (goes to earlier or same layer)
                val isBackEdge = toLayer <= fromLayer

                // Calculate orthogonal waypoints
                val waypoints = calculateOrthogonalWaypoints(
                    fromPosition,
                    toPosition,
                    isBackEdge
                )

                edgePaths.add(
                    EdgePath(
                        fromNode = node.organName,
                        toNode = edge.destinationName,
                        isBackEdge = isBackEdge,
                        waypoints = waypoints,
                        color = nodeColor // Edge color matches source node color
                    )
                )
            }
        }

        return edgePaths
    }

    /**
     * Calculates orthogonal waypoints (right-angle routing) between two nodes for vertical layout.
     */
    private fun calculateOrthogonalWaypoints(
        fromPosition: NodePosition,
        toPosition: NodePosition,
        isBackEdge: Boolean
    ): List<Offset> {
        val waypoints = mutableListOf<Offset>()

        // Start from bottom-center of source node
        val startX = fromPosition.x + (fromPosition.width / 2)
        val startY = fromPosition.y + fromPosition.height
        waypoints.add(Offset(startX, startY))

        // End at top-center of destination node
        val endX = toPosition.x + (toPosition.width / 2)
        val endY = toPosition.y

        if (isBackEdge) {
            // For back edges, route around to the side to avoid overlapping nodes
            val routeOffset = 60f
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
                // Nearly vertical - direct line
                waypoints.add(Offset(endX, endY))
            } else {
                // Use midpoint for right-angle routing
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
 * Calculates the angle in radians for an arrowhead pointing from (x1, y1) to (x2, y2).
 */
fun calculateArrowAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return atan2(y2 - y1, x2 - x1)
}
