package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Organ

class SmallIntestine: Organ(
    setOf(
        PancreaticAmylase(),
        Trypsin()
    )
)