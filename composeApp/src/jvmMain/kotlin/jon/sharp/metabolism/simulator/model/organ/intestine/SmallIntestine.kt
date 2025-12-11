package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType

class SmallIntestine: AbstractOrgan(
    setOf(
        PancreaticAmylase(),
        MembraneMaltoseHydrolysis()
    ),
    setOf(MetaboliteType.GLUCOSE)
)