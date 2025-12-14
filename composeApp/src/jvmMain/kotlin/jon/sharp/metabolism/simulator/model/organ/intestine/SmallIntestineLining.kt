package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class SmallIntestineLining: AbstractOrgan(
    setOf(
        MembraneMaltoseHydrolysis(),
        Peptidase()
    ),
    mapOf(
        MetaboliteType.GLUCOSE to PercentToOutput(50f),
        MetaboliteType.GLUTAMIC_ACID to PercentToOutput(50f)
    )
)
