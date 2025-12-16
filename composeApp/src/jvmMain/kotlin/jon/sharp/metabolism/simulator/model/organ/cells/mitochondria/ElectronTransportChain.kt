package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType

class ElectronTransportChain: Metabolizer(
    ode = EtcODE(),
    metaboliteTypes = listOf(
        MetaboliteType.NADH,
        MetaboliteType.FADH2,
        MetaboliteType.ATP
    )
) {
}