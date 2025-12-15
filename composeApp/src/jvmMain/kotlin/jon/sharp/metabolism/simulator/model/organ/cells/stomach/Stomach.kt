package jon.sharp.metabolism.simulator.model.organ.cells.stomach

import jon.sharp.metabolism.simulator.model.Organ

class Stomach: Organ(
    setOf(
        GastricAmylase(),
        Pepsin(),
    )
) {
}