package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.Organ

class MitochondrialMatrix(): Organ(
    setOf(
        AlanineAminoTransferase(),
        TCACycle(),
    )
) {
}