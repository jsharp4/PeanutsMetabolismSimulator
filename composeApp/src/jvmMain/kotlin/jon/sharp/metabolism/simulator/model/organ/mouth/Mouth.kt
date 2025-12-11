package jon.sharp.metabolism.simulator.model.organ.mouth

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants

class Mouth: AbstractOrgan(
    setOf(SalivaryAmylase()),
    setOf(MetaboliteType.STARCH, MetaboliteType.MALTOSE),
) {
}