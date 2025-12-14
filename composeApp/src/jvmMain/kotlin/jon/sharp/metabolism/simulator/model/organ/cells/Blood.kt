package jon.sharp.metabolism.simulator.model.organ.cells

import jon.sharp.metabolism.simulator.model.Organ
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants

/**
 * Blood organ stores and displays extracellular (blood) glucose concentration.
 *
 * In the pipeline, Blood appears twice:
 * 1. First position: Receives glucose from Small Intestine, passes to Cytosol
 * 2. Second position: Receives updated glucose from Cytosol for display
 *
 * Initial glucose concentration (5 mmol/L) is set in GlycolysisODESolver.
 * Performs no metabolism itself - purely for storage and passthrough.
 */
class Blood: Organ(
    metabolizers = setOf(),
    outputs = mapOf(),  // Outputs glucose to next organ in pipeline
)
