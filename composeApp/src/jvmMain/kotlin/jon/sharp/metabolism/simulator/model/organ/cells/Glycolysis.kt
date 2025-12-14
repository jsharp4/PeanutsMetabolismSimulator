package jon.sharp.metabolism.simulator.model.organ.cells

import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType

/**
 * Glycolysis metabolizer that breaks down glucose into pyruvate.
 *
 * Processes 8 metabolites in the glycolysis pathway:
 * 1. GLUCOSE (blood/extracellular)
 * 2. CELL_GLUCOSE (intracellular)
 * 3. G6P (Glucose-6-Phosphate)
 * 4. F6P (Fructose-6-Phosphate)
 * 5. F16BP (Fructose-1,6-Bisphosphate)
 * 6. e3PG (3-Phosphoglycerate)
 * 7. PEP (Phosphoenolpyruvate)
 * 8. PYRUVATE
 *
 * The solver handles all unit conversions internally, working with masses in mmol.
 */
class Glycolysis : Metabolizer(
    odeSolver = GlycolysisODESolver(),
    metaboliteTypes = listOf(
        MetaboliteType.GLUCOSE,
        MetaboliteType.CELL_GLUCOSE,
        MetaboliteType.G6P,
        MetaboliteType.F6P,
        MetaboliteType.F16BP,
        MetaboliteType.e3PG,
        MetaboliteType.PEP,
        MetaboliteType.PYRUVATE
    )
)