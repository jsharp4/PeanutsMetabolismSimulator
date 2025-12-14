package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class MitochondrialMatrix(): Organ(
    setOf(
        AlanineAminoTransferase(),
        TCACycle(),
        ),
    outputs = mapOf(
        MetaboliteType.GLUCOSE to PercentToOutput(100f),
        MetaboliteType.NADH to PercentToOutput(100f),
        MetaboliteType.FADH2 to PercentToOutput(100f)
    ),
) {
}