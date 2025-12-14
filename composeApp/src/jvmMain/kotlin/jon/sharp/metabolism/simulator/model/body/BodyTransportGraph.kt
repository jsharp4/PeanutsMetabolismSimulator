package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.organ.cells.Blood
import jon.sharp.metabolism.simulator.model.organ.cells.Cytosol
import jon.sharp.metabolism.simulator.model.organ.cells.mitochondria.MitochondrialInnerMembrane
import jon.sharp.metabolism.simulator.model.organ.cells.mitochondria.MitochondrialMatrix
import jon.sharp.metabolism.simulator.model.organ.cells.stomach.Stomach
import jon.sharp.metabolism.simulator.model.organ.intestine.SmallIntestine
import jon.sharp.metabolism.simulator.model.organ.intestine.SmallIntestineLining
import jon.sharp.metabolism.simulator.model.organ.mouth.Mouth

data class QuantifiedMetabolite(
    val type: MetaboliteType,
    val percentageOfOutput: Double
)

data class DirectionalOrganEdge(
    val destination: OrganNode,
    val metabolitesToSend: Set<QuantifiedMetabolite>
)

data class OrganNode(
    val organNode: Organ,
    val edges: Set<DirectionalOrganEdge>
)

class BodyTransportGraph {
    val mitochondrialInnerMembrane = MitochondrialInnerMembrane()
    val mitochondrialMatrix = MitochondrialMatrix()
    val cytosol = Cytosol()
    val blood = Blood()
    val intestineLining = SmallIntestineLining()
    val smallIntestine = SmallIntestine()
    val stomach = Stomach()
    val mouth = Mouth()


    val mitochondrialMembraneNode = OrganNode(
        mitochondrialInnerMembrane,
        setOf()
    )

    val mitochondrialMatrixNode = OrganNode(
        mitochondrialMatrix,
        setOf(
            DirectionalOrganEdge(
                mitochondrialMembraneNode,
                setOf(
                    QuantifiedMetabolite(
                    MetaboliteType.NADH,
                    100.0
                    )
                )
            )
        )
    )

    val cytosolNode = OrganNode(
        cytosol,
        setOf(
            DirectionalOrganEdge(
                mitochondrialMatrixNode,
                setOf(
                    QuantifiedMetabolite(
                        MetaboliteType.GLUCOSE,
                        100.0
                    ),
                    QuantifiedMetabolite(
                        MetaboliteType.PYRUVATE,
                        100.0
                    ),
                    QuantifiedMetabolite(
                        MetaboliteType.GLUTAMIC_ACID,
                        100.0
                    )
                )
            )

        )
    )

}