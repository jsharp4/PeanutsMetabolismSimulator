package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class MitochondrialInnerMembrane: AbstractOrgan(
    setOf(ElectronTransportChain()),
    mapOf(
        MetaboliteType.GLUCOSE to PercentToOutput(100f)
    ),
) {
}