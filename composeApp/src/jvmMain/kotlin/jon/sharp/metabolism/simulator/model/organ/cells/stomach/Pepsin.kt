package jon.sharp.metabolism.simulator.model.organ.cells.stomach

import jon.sharp.metabolism.simulator.model.AbstractMetabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType

class Pepsin: AbstractMetabolizer(
    jon.sharp.metabolism.simulator.model.AbstractODESolver(PepsinODE()),
    listOf(
        MetaboliteType.ARACHIN,
        MetaboliteType.POLYPEPTIDE
    )
)
// Need to add: Arachin, Polypeptides, Small peptide chains, Glutamate / Glutamic Acid
// Trypsin, Peptidase, aminotransferase to make a-ketoglutarate?
// NH4,  a-ketoglutarate?