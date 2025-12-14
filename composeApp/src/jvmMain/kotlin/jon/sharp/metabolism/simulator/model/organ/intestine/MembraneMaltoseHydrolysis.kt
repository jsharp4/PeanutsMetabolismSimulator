package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.ODESolver
import jon.sharp.metabolism.simulator.model.MaltoseHydrolysisODE
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Maltose.Hydrolysis.JEJUNUM_LENGTH_CM
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Maltose.Hydrolysis.LUMEN_VOLUME_LITERS
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Maltose.Hydrolysis.MAX_HYDROLYSIS_RATE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Maltose.Hydrolysis.MICHAELIS_CONSTANT

/**
 * Membrane-bound maltose hydrolysis in the small intestine.
 *
 * Processes 2 metabolites:
 * 1. MALTOSE (input, consumed)
 * 2. GLUCOSE (output, produced at 2:1 ratio)
 *
 * All values are in mmol.
 */
class MembraneMaltoseHydrolysis : Metabolizer(
    odeSolver = ODESolver(
        MaltoseHydrolysisODE(
            MAX_HYDROLYSIS_RATE,
            LUMEN_VOLUME_LITERS,
            JEJUNUM_LENGTH_CM,
            MICHAELIS_CONSTANT
        )
    ),
    metaboliteTypes = listOf(
        MetaboliteType.MALTOSE,
        MetaboliteType.GLUCOSE
    )
) {
    /**
     * Custom update to handle adding glucose to existing pool rather than replacing it,
     * since glucose may come from multiple sources.
     */
    override fun updateSubstrates(substrates: MetaboliteMap, state: DoubleArray) {
        // Update maltose (replace existing value)
        substrates.updateQuantities(
            Metabolite(MetaboliteType.MALTOSE, state[0].toFloat())
        )

        // Add produced glucose to existing pool
        substrates.putOrAdd(
            Metabolite(MetaboliteType.GLUCOSE, state[1].toFloat())
        )
    }
}