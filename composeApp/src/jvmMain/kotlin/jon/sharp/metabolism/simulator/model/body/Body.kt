package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.organ.IOrgan
import java.util.Queue
import kotlin.collections.set

actual class Body(
    private val transportGraph: BodyTransportGraph
) {

    actual val organNameMap: Map<String, IOrgan> =
        transportGraph.getAllOrgans().associateBy { it.getName() }

    actual fun getTransportGraph(): BodyTransportGraph {
        return transportGraph
    }

    var visitedEdgeMap = mutableMapOf<DirectionalOrganEdge, Int>()
    var visitedNodeMap = mutableMapOf<OrganNode, Int>()

    /**
     * Executes a metabolism time step by passing outputs through the organ pipeline.
     * Each organ processes metabolites and passes its outputs to the next organ in sequence.
     */
    actual suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap) {
        //var currentInputs = initialInputs
        visitedEdgeMap = mutableMapOf<DirectionalOrganEdge, Int>()
        visitedNodeMap = mutableMapOf<OrganNode, Int>()
        transportGraph.edgeTransportMap = mutableMapOf<DirectionalOrganEdge, MetaboliteMap>() // Reset transport map for new step
        val visitQueue = mutableListOf<Pair<OrganNode, MetaboliteMap>>()
        val first = transportGraph.getStartNode()

        visitQueue.add(Pair(first, initialInputs))

        while (visitQueue.isNotEmpty()) {
            val currNodePair = visitQueue.removeAt(0)

            if (!checkIfNodePreviouslyVisitedOrMarkAsVisited(currNodePair.first)) {

                currNodePair.first.organ.metabolizeTimeStep(currNodePair.second)

                populateQueueFromEdges(
                    currNodePair.first,
                    currNodePair.first.edges,
                    visitQueue
                )

            } else {
                currNodePair.first.organ.metabolitesMap.putOrAdd(currNodePair.second)
            }
        }
    }

    private fun populateQueueFromEdges(
        currentNode: OrganNode,
        edges: Set<DirectionalOrganEdge>,
        queue: MutableList<Pair<OrganNode, MetaboliteMap>>
    ) {
        val toRemoveFromCurrentNode = MetaboliteMap()
        edges.forEach { edge ->
            if (!checkIfEdgePreviouslyVisitedOrMarkAsVisited(edge)) {
                // Create MetaboliteMap to capture transported amounts for visualization
                val transportedMetabolites = MetaboliteMap()

                val metabolitesToQueue = MetaboliteMap(
                    edge.metabolitesToSend.map { metabolite ->
                        val actualAmount = metabolite.percentageOfOutput / 100 *
                                getMetaboliteFromOrganOrZero(currentNode.organ, metabolite.type)

                        val transportedMetabolite = Metabolite(metabolite.type, actualAmount)

                        toRemoveFromCurrentNode.putOrAdd(transportedMetabolite)
                        transportedMetabolites.putOrAdd(transportedMetabolite) // Capture for visualization

                        transportedMetabolite
                    }.toSet()
                )

                // Store in transport graph for visualization
                transportGraph.edgeTransportMap[edge] = transportedMetabolites

                queue.add(Pair(edge.destination, metabolitesToQueue))
            }
        }
        currentNode.organ.metabolitesMap.removeIfPresent(toRemoveFromCurrentNode)
    }

    private fun checkIfNodePreviouslyVisitedOrMarkAsVisited(node: OrganNode): Boolean {
        val previouslyVisited = (visitedNodeMap[node] ?: 0) > 0
        if (!previouslyVisited) visitedNodeMap[node] = 1
        return previouslyVisited
    }

    private fun checkIfEdgePreviouslyVisitedOrMarkAsVisited(edge: DirectionalOrganEdge): Boolean {
        val previouslyVisited = (visitedEdgeMap[edge] ?: 0) > 0
        if (!previouslyVisited) visitedEdgeMap[edge] = 1
        return previouslyVisited
    }

    private fun getMetaboliteFromOrganOrZero(organ: Organ, type: MetaboliteType): Float {
        return organ.getMetabolites()[type]?.amountMilliMoles ?: 0f
    }

    /**
     * Gets the current metabolites for a specific organ by name.
     */
//    actual fun getOrganMetabolites(organName: String): MetaboliteMap {
//        return organs.find { it.first == organName }?.second?.getMetabolites() ?: MetaboliteMap()
//    }

    /**
     * Returns the list of all organ names in pipeline order.
     */
//    actual fun getOrganNames(): List<String> {
//        return organs.map { it.first }
//    }
}