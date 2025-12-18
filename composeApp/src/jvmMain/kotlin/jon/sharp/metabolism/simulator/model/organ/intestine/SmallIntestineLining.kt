package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Organ

class SmallIntestineLining: Organ(
    listOf(
        MembraneMaltoseHydrolysis(),
        Peptidase(),
        MixedMicelleFormer()
    )
)
