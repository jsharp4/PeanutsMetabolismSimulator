package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType

class MitochondrialMatrix(): AbstractOrgan(
    setOf(TCACycle()),
    outputs = setOf(
        MetaboliteType.GLUCOSE,
        MetaboliteType.NADH,
        MetaboliteType.FADH2
    ),
) {
}