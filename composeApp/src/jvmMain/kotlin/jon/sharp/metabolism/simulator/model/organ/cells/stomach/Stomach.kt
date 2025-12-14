package jon.sharp.metabolism.simulator.model.organ.cells.stomach

import jon.sharp.metabolism.simulator.model.AbstractOrgan
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class Stomach: AbstractOrgan(
    setOf(
        GastricAmylase(),
        Pepsin(),
    ),
    mapOf(
        MetaboliteType.STARCH to PercentToOutput(30f),
        MetaboliteType.MALTOSE to PercentToOutput(30f),
        MetaboliteType.POLYPEPTIDE to PercentToOutput(100f)
    )
) {
}