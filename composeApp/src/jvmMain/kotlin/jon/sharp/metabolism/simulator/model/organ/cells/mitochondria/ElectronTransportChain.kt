package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType

class ElectronTransportChain: Metabolizer(
    jon.sharp.metabolism.simulator.model.ODESolver(EtcODE()),
    listOf(
        MetaboliteType.NADH,
        MetaboliteType.FADH2,
        MetaboliteType.ATP
    )
) {
}