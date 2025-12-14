package jon.sharp.metabolism.simulator.model.organ.mouth

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput
import jon.sharp.metabolism.simulator.model.PhysicalConstants

class Mouth: Organ(
    setOf(SalivaryAmylase()),
    mapOf(
        MetaboliteType.STARCH to PercentToOutput(100f),
        MetaboliteType.MALTOSE to PercentToOutput(100f),
        MetaboliteType.ARACHIN to PercentToOutput(100f),
    ),
) {
}