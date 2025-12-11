package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType

class MitochondrialInnerMembrane: AbstractOrgan(
    setOf(ElectronTransportChain()),
    setOf(MetaboliteType.GLUCOSE),
) {
}