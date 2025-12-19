package jon.sharp.metabolism.simulator.model.organ.pancreas

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ

class Pancreas: Organ(
    listOf(InsulinProduction()),
        readOnlyMetabolites = setOf(MetaboliteType.GLUCOSE, MetaboliteType.GLUTAMIC_ACID)
) {
}