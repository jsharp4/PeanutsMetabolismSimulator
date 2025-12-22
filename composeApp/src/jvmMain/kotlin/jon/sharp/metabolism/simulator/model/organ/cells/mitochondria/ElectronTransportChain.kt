package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.MetabolicProcess
import jon.sharp.metabolism.simulator.model.MetaboliteType

class ElectronTransportChain: MetabolicProcess(
    ode = EtcODE(),
    metaboliteTypes = listOf(
        MetaboliteType.NADH,
        MetaboliteType.FADH2,
        MetaboliteType.ATP
    )
) {
}