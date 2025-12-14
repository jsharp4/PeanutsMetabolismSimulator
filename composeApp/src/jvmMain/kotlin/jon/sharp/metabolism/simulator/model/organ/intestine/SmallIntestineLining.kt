package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class SmallIntestineLining: Organ(
    setOf(
        MembraneMaltoseHydrolysis(),
        Peptidase()
    ),
    mapOf(
        MetaboliteType.GLUCOSE to PercentToOutput(50f),
        MetaboliteType.GLUTAMIC_ACID to PercentToOutput(50f)
    )
)
