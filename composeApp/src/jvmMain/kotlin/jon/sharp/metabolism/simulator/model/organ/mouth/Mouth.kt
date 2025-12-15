package jon.sharp.metabolism.simulator.model.organ.mouth

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants

class Mouth: Organ(
    setOf(SalivaryAmylase())
) {
}