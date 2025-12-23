package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.UpdateType
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
    actual suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap, t0: Double) {
        //var currentInputs = initialInputs
        visitedEdgeMap = mutableMapOf<DirectionalOrganEdge, Int>()
        visitedNodeMap = mutableMapOf<OrganNode, Int>()
        // Build transport map locally to avoid UI seeing partial state during step
        val newEdgeTransportMap = mutableMapOf<DirectionalOrganEdge, MetaboliteMap>()
        val visitQueue = mutableListOf<Pair<OrganNode, MetaboliteMap>>()
        val first = transportGraph.getStartNode()

        visitQueue.add(Pair(first, initialInputs))

        while (visitQueue.isNotEmpty()) {
            val currNodePair = visitQueue.removeAt(0)

            if (!checkIfNodePreviouslyVisitedOrMarkAsVisited(currNodePair.first)) {

                currNodePair.first.organ.metabolizeTimeStep(currNodePair.second, t0)

                populateQueueFromEdges(
                    currNodePair.first,
                    currNodePair.first.edges,
                    visitQueue,
                    newEdgeTransportMap,
                    t0
                )

            } else {
                currNodePair.second.getAll().forEach {
                    it ->
                    if (it.updateType == UpdateType.OVERWRITE) {
                        currNodePair.first.organ.metabolitesMap.updateQuantities(it)
                    }
                    else currNodePair.first.organ.metabolitesMap.putOrAdd(it)
                }
                //currNodePair.first.organ.metabolitesMap.putOrAdd(currNodePair.second)
            }
        }

        // Atomically update the transport map so UI sees complete state
        transportGraph.edgeTransportMap = newEdgeTransportMap
    }

    private fun populateQueueFromEdges(
        currentNode: OrganNode,
        edges: Set<DirectionalOrganEdge>,
        queue: MutableList<Pair<OrganNode, MetaboliteMap>>,
        edgeTransportMap: MutableMap<DirectionalOrganEdge, MetaboliteMap>,
        t0: Double
    ) {
        val toRemoveFromCurrentNode = MetaboliteMap()
        edges.forEach { edge ->
            if (!checkIfEdgePreviouslyVisitedOrMarkAsVisited(edge)) {
                // Create MetaboliteMap to capture transported amounts for visualization
                val transportedMetabolites = MetaboliteMap()

                val rateLimitedRetainedMetabolites = MetaboliteMap()
                edge.rateLimiter.forEach {
                    rateLimitedRetainedMetabolites.updateQuantities(
                        it.processSubstrates(currentNode.organ.metabolitesMap, t0)
                    )
                }

                val rateLimitedOutputMetabolites = currentNode.organ.metabolitesMap.copy()
                if (edge.rateLimiter.isNotEmpty()) {
                    rateLimitedOutputMetabolites.removeIfPresent(rateLimitedRetainedMetabolites)
                }

                val metabolitesToQueue = MetaboliteMap(
                    edge.metabolitesToSend.map { metabolite ->

                        val actualAmount = metabolite.percentageOfOutput / 100 *
                                getMetaboliteFromMapOrZero(
                                    rateLimitedOutputMetabolites,
                                    metabolite.type)

                        var metaboliteUpdateType = UpdateType.ACCUMULATE

                        if (edge.type == EdgeType.OVERWRITE || edge.type == EdgeType.READ_ONLY) {
                            metaboliteUpdateType = UpdateType.OVERWRITE
                        }

                        val transportedMetabolite = Metabolite(
                            metabolite.type,
                            actualAmount,
                            updateType = metaboliteUpdateType
                        )

                        if (edge.type == EdgeType.TRANSFER) {
                            toRemoveFromCurrentNode.putOrAdd(transportedMetabolite)
                        }
                        transportedMetabolites.putOrAdd(transportedMetabolite) // Capture for visualization

                        transportedMetabolite
                    }.toSet()
                )

                // Store in local transport map for visualization
                edgeTransportMap[edge] = transportedMetabolites

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

    private fun getMetaboliteFromMapOrZero(map: MetaboliteMap, type: MetaboliteType): Double {
        return map[type]?.amountMilliMoles ?: 0.0
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