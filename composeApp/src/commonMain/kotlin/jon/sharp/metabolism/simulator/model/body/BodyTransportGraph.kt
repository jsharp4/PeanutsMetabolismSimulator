package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.organ.IOrgan

data class GraphEdge(
    val destinationName: String,
    val metabolites: List<MetaboliteTransfer>
)

data class MetaboliteTransfer(
    val type: MetaboliteType,
    val percentage: Double,
    val actualAmount: Double = 0.0 // Actual amount transported in last step
)

data class GraphNode(
    val organName: String,
    val edges: List<GraphEdge>,
    val rowNumber: Int
)

/**
 * Configuration for organ layout in the graph visualization.
 * Row numbers determine vertical position (0 = top).
 * Column numbers determine horizontal position within a row (0 = leftmost).
 */
object OrganLayoutConfig {
    data class LayoutPosition(val row: Int, val column: Int)

    val organPositions: Map<String, LayoutPosition> = mapOf(
        "Mouth" to LayoutPosition(row = 0, column = 0),
        "Stomach" to LayoutPosition(row = 1, column = 0),
        "SmallIntestine" to LayoutPosition(row = 2, column = 0),
        "SmallIntestineLining" to LayoutPosition(row = 3, column = 0),
        "Enterocytes" to LayoutPosition(row = 3, column = 1),
        "Blood" to LayoutPosition(row = 4, column = 0),
        "Liver" to LayoutPosition(row = 4, column = 1),
        "Pancreas" to LayoutPosition(row = 5, column = 1),
        "Cytosol" to LayoutPosition(row = 5, column = 0),
        "MitochondrialMatrix" to LayoutPosition(row = 6, column = 0),
        "MitochondrialInnerMembrane" to LayoutPosition(row = 7, column = 0)
    )

    fun getRowNumber(organName: String): Int =
        organPositions[organName]?.row ?: 0

    fun getColumnNumber(organName: String): Int =
        organPositions[organName]?.column ?: 0

    /**
     * Returns a map of organ names to their column numbers for use by the graph visualization.
     */
    fun getColumnMap(): Map<String, Int> =
        organPositions.mapValues { it.value.column }
}

expect class BodyTransportGraph {
    fun getAllOrgans(): Set<IOrgan>
    fun getGraphStructure(): List<GraphNode>
}