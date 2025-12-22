package jon.sharp.metabolism.simulator.model.organ.stomach

import jon.sharp.metabolism.simulator.model.MetabolicProcess
import jon.sharp.metabolism.simulator.model.MetaboliteType

class Pepsin: MetabolicProcess(
    ode = PepsinODE(),
    metaboliteTypes = listOf(
        MetaboliteType.ARACHIN,
        MetaboliteType.POLYPEPTIDE
    )
)
// Need to add: Arachin, Polypeptides, Small peptide chains, Glutamate / Glutamic Acid
// Trypsin, Peptidase, aminotransferase to make a-ketoglutarate?
// NH4,  a-ketoglutarate?