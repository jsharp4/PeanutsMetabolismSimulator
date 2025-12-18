package jon.sharp.metabolism.simulator.model.organ.cells

import jon.sharp.metabolism.simulator.model.Organ

/**
 * Cytosol organ manages intracellular glucose metabolism through glycolysis.
 * Handles the complete glycolysis ODE system (y[0-7]):
 * - y[0]: Extracellular (blood) glucose
 * - y[1-7]: Intracellular metabolites (glucose, G6P, F6P, F16BP, 3PG, PEP, pyruvate)
 *
 * Receives glucose from Small Intestine and outputs updated blood glucose to Blood organ.
 */
class Cytosol: Organ(
    metabolizers = listOf(
        Glycolysis(),
        AcylCoASynthase()
    )
)
