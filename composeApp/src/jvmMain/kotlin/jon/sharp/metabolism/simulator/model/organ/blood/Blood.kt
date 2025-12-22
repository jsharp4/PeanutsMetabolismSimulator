package jon.sharp.metabolism.simulator.model.organ.blood

import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.PhysicalConstants

class Blood: Organ(
    metabolicProcesses = listOf(
        LipoproteinLipase(),
        //InsulinBreakdown()
    ),
    MetaboliteMap(
        Metabolite(
            MetaboliteType.GLUCOSE,
            5.0 * PhysicalConstants.Blood.TOTAL_LITERS
        ),
        Metabolite(
            MetaboliteType.INSULIN,
            PhysicalConstants.Insulin.internationalUnitConcToMmol(
                PhysicalConstants.Blood.FastingValues.basalInsulin
            )
        ),
        Metabolite(
            MetaboliteType.GLUTAMIC_ACID,
            PhysicalConstants.Blood.FastingValues.basalBCAA * PhysicalConstants.Blood.TOTAL_LITERS
        )
    )
)