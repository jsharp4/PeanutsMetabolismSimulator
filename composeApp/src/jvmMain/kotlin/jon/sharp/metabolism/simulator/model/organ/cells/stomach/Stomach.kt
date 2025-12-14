package jon.sharp.metabolism.simulator.model.organ.cells.stomach

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class Stomach: Organ(
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