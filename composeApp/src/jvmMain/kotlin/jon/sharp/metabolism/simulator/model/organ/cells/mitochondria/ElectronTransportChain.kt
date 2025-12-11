package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.AbstractMetabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType

class ElectronTransportChain: AbstractMetabolizer(
    EtcODESolver(),
    listOf(
        MetaboliteType.NADH,
        MetaboliteType.FADH2,
        MetaboliteType.ATP
    )
) {
}