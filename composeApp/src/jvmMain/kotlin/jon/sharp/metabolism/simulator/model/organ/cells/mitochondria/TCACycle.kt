package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.AbstractMetabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType

/**
 * TCA Cycle (Citric Acid Cycle / Krebs Cycle) metabolizer.
 *
 * Processes 4 metabolites:
 * 1. PYRUVATE (input, consumed)
 * 2. NADH (output, produced)
 * 3. FADH2 (output, produced)
 * 4. GTP (output, produced)
 *
 * All values are in mmol.
 */
class TCACycle : AbstractMetabolizer(
    odeSolver = TcaODESolver(),
    metaboliteTypes = listOf(
        MetaboliteType.PYRUVATE,
        MetaboliteType.NADH,
        MetaboliteType.FADH2,
        MetaboliteType.GTP
    )
)