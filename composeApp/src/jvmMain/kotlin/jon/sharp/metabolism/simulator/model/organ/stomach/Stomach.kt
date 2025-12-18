package jon.sharp.metabolism.simulator.model.organ.stomach

import jon.sharp.metabolism.simulator.model.Organ

class Stomach: Organ(
    listOf(
        GastricAmylase(),
        Pepsin(),
    )
) {
}