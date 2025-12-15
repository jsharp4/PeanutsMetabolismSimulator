package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.organ.IOrgan

data class GraphEdge(
    val destinationName: String,
    val metabolites: List<MetaboliteTransfer>
)

data class MetaboliteTransfer(
    val type: MetaboliteType,
    val percentage: Float
)

data class GraphNode(
    val organName: String,
    val edges: List<GraphEdge>
)

expect class BodyTransportGraph {
    fun getAllOrgans(): Set<IOrgan>
    fun getGraphStructure(): List<GraphNode>
}