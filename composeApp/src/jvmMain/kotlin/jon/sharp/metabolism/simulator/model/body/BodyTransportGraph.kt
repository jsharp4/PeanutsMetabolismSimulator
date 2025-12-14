package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ

data class QuantifiedMetabolite(
    val type: MetaboliteType,
    val percentageOfOutput: Double
)

data class OrganNode(
    val organNode: Organ,

)

class BodyTransportGraph {

}