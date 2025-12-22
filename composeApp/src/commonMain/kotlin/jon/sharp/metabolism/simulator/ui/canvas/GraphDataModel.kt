package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.body.Body
import jon.sharp.metabolism.simulator.model.body.BodyTransportGraph
import jon.sharp.metabolism.simulator.model.body.GraphEdge
import jon.sharp.metabolism.simulator.model.body.MetaboliteTransfer

/**
 * Represents a graph node with its metabolite contents merged in.
 */
data class GraphNodeWithMetabolites(
    val organName: String,
    val edges: List<GraphEdge>,
    val metabolites: List<Metabolite>,
    val rowNumber: Int
)

/**
 * Represents a positioned node on the canvas.
 */
data class NodePosition(
    val organName: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color
)

/**
 * Represents an edge path on the canvas using orthogonal routing.
 */
data class EdgePath(
    val fromNode: String,
    val toNode: String,
    val isBackEdge: Boolean,
    val waypoints: List<Offset>, // List of points for orthogonal routing (start → intermediate points → end)
    val color: Color, // Color matches the source node
    val metabolites: List<MetaboliteTransfer> = emptyList() // Metabolite data for labels
)

/**
 * Complete layout result containing all positioned elements.
 */
data class LayoutResult(
    val nodePositions: Map<String, NodePosition>,
    val edgePaths: List<EdgePath>,
    val canvasWidth: Float,
    val canvasHeight: Float
)

/**
 * Merges graph structure with metabolite data from the body.
 */
fun createGraphWithMetabolites(
    transportGraph: BodyTransportGraph,
    body: Body
): List<GraphNodeWithMetabolites> {
    val graphStructure = transportGraph.getGraphStructure()

    return graphStructure.map { graphNode ->
        val organ = body.organNameMap[graphNode.organName]
            ?: error("Organ ${graphNode.organName} not found in body")

        val metabolites = organ.getMetabolites().getAll().toList()

        GraphNodeWithMetabolites(
            organName = graphNode.organName,
            edges = graphNode.edges,
            metabolites = metabolites,
            rowNumber = graphNode.rowNumber
        )
    }
}
