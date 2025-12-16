package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.organ.IOrgan

actual class Body(
    private val transportGraph: BodyTransportGraph
) {

    actual val organNameMap: Map<String, IOrgan> =
        transportGraph.getAllOrgans().associateBy { it.getName() }

    actual fun getTransportGraph(): BodyTransportGraph {
        return transportGraph
    }

    /**
     * Executes a metabolism time step by passing outputs through the organ pipeline.
     * Each organ processes metabolites and passes its outputs to the next organ in sequence.
     */
    actual suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap) {
        //var currentInputs = initialInputs
        val visitedEdgeMap = mutableMapOf<DirectionalOrganEdge, Int>()
        val visitedNodeMap = mutableMapOf<OrganNode, Int>()
        val visitQueue = mutableListOf<Pair<OrganNode, MetaboliteMap>>()
        val first = transportGraph.getStartNode()

        visitQueue.add(Pair(first, initialInputs))

        while (visitQueue.isNotEmpty()) {
            val currNodePair = visitQueue.removeAt(0)

            if ((visitedNodeMap[currNodePair.first] ?: 0) < 1) {
                visitedNodeMap[currNodePair.first] = 1
                currNodePair.first.organ.metabolizeTimeStep(currNodePair.second)

                val toRemoveFromCurrentNode = MetaboliteMap()
                currNodePair.first.edges.forEach { edge ->
                    if ((visitedEdgeMap[edge] ?: 0) < 1) {
                        visitedEdgeMap[edge] = 1
                        visitQueue.add(
                            Pair(
                                edge.destination,
                                MetaboliteMap(
                                    edge.metabolitesToSend.map { metabolite ->
                                        toRemoveFromCurrentNode.putOrAdd(
                                            Metabolite(
                                                metabolite.type,
                                                metabolite.percentageOfOutput *
                                                        currNodePair.first.organ.getMetabolites()[metabolite.type]!!.amountMilliMoles
                                            )
                                        )
                                        Metabolite(
                                            metabolite.type,
                                            metabolite.percentageOfOutput *
                                                    currNodePair.first.organ.getMetabolites()[metabolite.type]!!.amountMilliMoles
                                        )
                                    }.toSet()
                                )
                            )
                        )
                    }
                }
                currNodePair.first.organ.metabolitesMap.removeIfPresent(toRemoveFromCurrentNode)
            } else {
                currNodePair.first.organ.metabolitesMap.putOrAdd(currNodePair.second)
            }


            //organs.forEach { (_, organ) ->
            //    currentInputs = organ.metabolizeTimeStep(currentInputs)
            //delay(500L)
            //}
        }
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