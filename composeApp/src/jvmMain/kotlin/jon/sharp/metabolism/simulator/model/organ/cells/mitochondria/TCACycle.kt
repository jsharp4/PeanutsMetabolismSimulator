package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.Metabolizer
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
class TCACycle : Metabolizer(
    ode = TcaODE(),
    metaboliteTypes = listOf(
        MetaboliteType.PYRUVATE,
        MetaboliteType.NADH,
        MetaboliteType.FADH2,
        MetaboliteType.GTP,
        MetaboliteType.KETOGLUTARATE,
        MetaboliteType.ACETYL_COA
    )
)