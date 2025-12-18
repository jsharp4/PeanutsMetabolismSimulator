package jon.sharp.metabolism.simulator.model.organ.pancreas

import jon.sharp.metabolism.simulator.model.Organ

class Pancreas: Organ(
    listOf(InsulinProduction()
    )
) {
}