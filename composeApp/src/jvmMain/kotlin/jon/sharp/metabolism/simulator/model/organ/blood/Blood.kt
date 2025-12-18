package jon.sharp.metabolism.simulator.model.organ.blood

import jon.sharp.metabolism.simulator.model.Organ

class Blood: Organ(
    metabolizers = setOf(
        LipoproteinLipase()
    )
)