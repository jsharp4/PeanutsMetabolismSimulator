package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PercentToOutput

class SmallIntestine: Organ(
    setOf(
        PancreaticAmylase(),
        Trypsin()
    ),
    mapOf(
        MetaboliteType.MALTOSE to PercentToOutput(50f),
        MetaboliteType.PEPTIDE_CHAIN to PercentToOutput(50f)
    )
)