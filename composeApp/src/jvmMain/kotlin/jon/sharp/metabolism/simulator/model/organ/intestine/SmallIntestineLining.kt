package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Organ

class SmallIntestineLining: Organ(
    setOf(
        MembraneMaltoseHydrolysis(),
        Peptidase(),
        MixedMicelleFormer()
    )
)
