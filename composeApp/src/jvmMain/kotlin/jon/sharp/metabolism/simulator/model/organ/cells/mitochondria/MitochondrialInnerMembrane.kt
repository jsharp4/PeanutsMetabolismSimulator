package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class MitochondrialInnerMembrane: Organ(
    setOf(ElectronTransportChain()),
    mapOf(
        MetaboliteType.GLUCOSE to PercentToOutput(100f)
    ),
) {
}