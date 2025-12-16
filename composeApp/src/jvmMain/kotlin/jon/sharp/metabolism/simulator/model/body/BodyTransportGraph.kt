package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.organ.IOrgan
import jon.sharp.metabolism.simulator.model.organ.cells.Blood
import jon.sharp.metabolism.simulator.model.organ.cells.Cytosol
import jon.sharp.metabolism.simulator.model.organ.cells.mitochondria.MitochondrialInnerMembrane
import jon.sharp.metabolism.simulator.model.organ.cells.mitochondria.MitochondrialMatrix
import jon.sharp.metabolism.simulator.model.organ.cells.stomach.Stomach
import jon.sharp.metabolism.simulator.model.organ.intestine.SmallIntestine
import jon.sharp.metabolism.simulator.model.organ.intestine.SmallIntestineLining
import jon.sharp.metabolism.simulator.model.organ.liver.Liver
import jon.sharp.metabolism.simulator.model.organ.mouth.Mouth

data class QuantifiedMetabolite(
    val type: MetaboliteType,
    val percentageOfOutput: Float
)

class DirectionalOrganEdge(
    val destination: OrganNode,
    val metabolitesToSend: Set<QuantifiedMetabolite>
) {
    // Varargs constructor for convenience
    constructor(
        destination: OrganNode,
        vararg metabolitesToSend: QuantifiedMetabolite
    ) : this(destination, metabolitesToSend.toSet())

    // Use object identity to avoid infinite recursion in circular graphs
    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = this === other
}

class OrganNode(
    val organ: Organ,
    var edges: Set<DirectionalOrganEdge>
) {
    // Varargs constructor for convenience
    constructor(
        organ: Organ,
        vararg edges: DirectionalOrganEdge
    ) : this(organ, edges.toSet())

    // Use organ's identity for hashCode to avoid infinite recursion in circular graphs
    override fun hashCode(): Int = System.identityHashCode(organ)
    override fun equals(other: Any?): Boolean = other is OrganNode && other.organ === this.organ
}

actual class BodyTransportGraph {
    val mitochondrialInnerMembrane = MitochondrialInnerMembrane()
    val mitochondrialMatrix = MitochondrialMatrix()
    val cytosol = Cytosol()
    val blood = Blood()
    val intestineLining = SmallIntestineLining()
    val smallIntestine = SmallIntestine()
    val stomach = Stomach()
    val mouth = Mouth()
    val liver = Liver()

    val mouthNode: OrganNode

    private val allOrgans = setOf(
        mouth,
        stomach,
        smallIntestine,
        liver,
        intestineLining,
        blood,
        cytosol,
        mitochondrialMatrix,
        mitochondrialInnerMembrane
    )

    init {
        val bloodNode: OrganNode = OrganNode(blood)

        val mitochondrialMembraneNode = OrganNode(mitochondrialInnerMembrane)

        val mitochondrialMatrixNode = OrganNode(
            mitochondrialMatrix,
            DirectionalOrganEdge(
                mitochondrialMembraneNode,
                QuantifiedMetabolite(MetaboliteType.NADH, 100.0f),
                QuantifiedMetabolite(MetaboliteType.FADH2, 100.0f)
            )
        )

        val cytosolNode: OrganNode = OrganNode(
            cytosol,
            DirectionalOrganEdge(
                mitochondrialMatrixNode,
                QuantifiedMetabolite(MetaboliteType.PYRUVATE, 100.0f),
                QuantifiedMetabolite(MetaboliteType.GLUTAMIC_ACID, 100.0f)
            ),
            DirectionalOrganEdge(
                bloodNode,
                QuantifiedMetabolite(MetaboliteType.GLUCOSE, 100.0f)
            )
        )

        val intestineLiningNode = OrganNode(
            intestineLining,
            DirectionalOrganEdge(
                bloodNode,
                QuantifiedMetabolite(MetaboliteType.GLUTAMIC_ACID, 50.0f),
                QuantifiedMetabolite(MetaboliteType.GLUCOSE, 50.0f)
            )
        )

        val smallIntestineNode = OrganNode(
            smallIntestine,
            DirectionalOrganEdge(
                intestineLiningNode,
                QuantifiedMetabolite(MetaboliteType.MALTOSE, 50.0f),
                QuantifiedMetabolite(MetaboliteType.PEPTIDE_CHAIN, 50.0f),
                QuantifiedMetabolite(MetaboliteType.OLEIC_ACID, 50f),
                QuantifiedMetabolite(MetaboliteType.GLYCEROL, 50f)
            )
        )

        val liverNode = OrganNode(
            liver,
            DirectionalOrganEdge(
                smallIntestineNode,
                QuantifiedMetabolite(MetaboliteType.BILE_SALT, 30f)
            )
        )


        val stomachNode = OrganNode(
            stomach,
            DirectionalOrganEdge(
                smallIntestineNode,
                QuantifiedMetabolite(MetaboliteType.STARCH, 30.0f),
                QuantifiedMetabolite(MetaboliteType.MALTOSE, 30.0f),
                QuantifiedMetabolite(MetaboliteType.POLYPEPTIDE, 100.0f),
                QuantifiedMetabolite(MetaboliteType.TRIOLEIN, 100f)
            )
        )

        mouthNode = OrganNode(
            mouth,
            DirectionalOrganEdge(
                stomachNode,
                QuantifiedMetabolite(MetaboliteType.STARCH, 100.0f),
                QuantifiedMetabolite(MetaboliteType.MALTOSE, 100.0f),
                QuantifiedMetabolite(MetaboliteType.ARACHIN, 100.0f),
                QuantifiedMetabolite(MetaboliteType.TRIOLEIN, 100f)
            )
        )
        bloodNode.edges = setOf(
            DirectionalOrganEdge(
                cytosolNode,
                QuantifiedMetabolite(MetaboliteType.GLUCOSE, 100f),
                QuantifiedMetabolite(MetaboliteType.GLUTAMIC_ACID, 100f)
            ),
            DirectionalOrganEdge(liverNode)
        )
    }

    actual fun getAllOrgans(): Set<IOrgan> {
        return allOrgans
    }

    fun getStartNode(): OrganNode {
        return mouthNode
    }

    actual fun getGraphStructure(): List<GraphNode> {
        // Build a map of organ to node for lookup
        val allNodes = collectAllNodes(mouthNode)
        val organToName = allOrgans.associate { organ ->
            organ to when (organ) {
                mouth -> "Mouth"
                stomach -> "Stomach"
                smallIntestine -> "Small Intestine"
                intestineLining -> "Small Intestine Lining"
                cytosol -> "Cytosol"
                mitochondrialMatrix -> "Mitochondrial Matrix"
                mitochondrialInnerMembrane -> "Inner Matrix Membrane"
                blood -> "Blood"
                else -> organ.getName()
            }
        }

        return allNodes.map { node ->
            GraphNode(
                organName = organToName[node.organ] ?: node.organ.getName(),
                edges = node.edges.map { edge ->
                    GraphEdge(
                        destinationName = organToName[edge.destination.organ] ?: edge.destination.organ.getName(),
                        metabolites = edge.metabolitesToSend.map { metabolite ->
                            MetaboliteTransfer(
                                type = metabolite.type,
                                percentage = metabolite.percentageOfOutput
                            )
                        }
                    )
                }
            )
        }
    }

    private fun collectAllNodes(startNode: OrganNode): Set<OrganNode> {
        val visited = mutableSetOf<OrganNode>()
        val queue = mutableListOf(startNode)

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            if (current !in visited) {
                visited.add(current)
                queue.addAll(current.edges.map { it.destination })
            }
        }

        return visited
    }

}