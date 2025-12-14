package jon.sharp.metabolism.simulator.model

/**
 * Abstract base class for ODE-based metabolizers that provides common processing logic.
 *
 * This class handles the standard pattern of:
 * 1. Extracting metabolite masses from the input map based on ordered types
 * 2. Running the ODE solver to simulate the metabolic process
 * 3. Updating the substrate map with the results
 *
 * Subclasses can override template methods for custom behavior while maintaining
 * the overall structure.
 *
 * @param odeSolver The ODE solver that implements the metabolic equations
 * @param metaboliteTypes Ordered list of metabolite types corresponding to the solver's state vector
 */
abstract class Metabolizer(
    protected val odeSolver: ODESolver,
    protected val metaboliteTypes: List<MetaboliteType>
) : IMetabolizer {

    override fun processSubstrates(inputs: MetaboliteMap): MetaboliteMap {
        val newSubstrates = inputs.copy()

        val initialState = extractState(newSubstrates)
        val finalState = odeSolver.stepForwardOneMinute(initialState)
        updateSubstrates(newSubstrates, finalState)

        return newSubstrates
    }

    /**
     * Extracts the state vector from the metabolite map.
     * Default implementation extracts masses (in mmol) in the order specified by metaboliteTypes.
     * Override for custom extraction logic.
     *
     * @param inputs The metabolite map to extract state from
     * @return State vector with values in the same order as metaboliteTypes
     */
    protected open fun extractState(inputs: MetaboliteMap): DoubleArray {
        return metaboliteTypes.map { type ->
            inputs[type]?.amountMilliMoles?.toDouble() ?: 0.0
        }.toDoubleArray()
    }

    /**
     * Updates the substrate map with the results from the solver.
     * Default implementation updates metabolites based on the ordered types.
     * Override for custom update logic.
     *
     * @param substrates The metabolite map to update
     * @param state The final state vector from the solver
     */
    protected open fun updateSubstrates(substrates: MetaboliteMap, state: DoubleArray) {
        val updatedMetabolites = metaboliteTypes.mapIndexed { index, type ->
            Metabolite(type, state[index].toFloat())
        }.toTypedArray()
        substrates.updateQuantities(*updatedMetabolites)
    }
}
