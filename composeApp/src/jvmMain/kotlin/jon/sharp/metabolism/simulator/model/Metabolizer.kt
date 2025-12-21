package jon.sharp.metabolism.simulator.model

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

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
 * @param ode The differential equations that define the metabolic process
 * @param metaboliteTypes Ordered list of metabolite types corresponding to the solver's state vector
 */
abstract class Metabolizer(
    ode: FirstOrderDifferentialEquations,
    protected val metaboliteTypes: List<MetaboliteType>
) : IMetabolizer {

    /**
     * The ODE solver that implements the metabolic equations.
     * Created by calling createSolver() with the provided differential equations.
     */
    protected val odeSolver: ODESolver = createSolver(ode)

    /**
     * Creates the ODE solver for this metabolizer.
     * Default implementation creates a standard ODESolver.
     * Override this method to provide a custom solver implementation.
     *
     * @param ode The differential equations to solve
     * @return An ODESolver instance
     */
    protected open fun createSolver(ode: FirstOrderDifferentialEquations): ODESolver {
        return ODESolver(ode)
    }

    override fun processSubstrates(inputs: MetaboliteMap, t0: Double): MetaboliteMap {
        val newSubstrates = inputs.copy()

        val initialState = extractState(newSubstrates)
        val finalState = odeSolver.stepForwardOneMinute(initialState, t0)
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
            inputs[type]?.amountMilliMoles ?: 0.0
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
            Metabolite(type, state[index])
        }.toTypedArray()
        substrates.updateQuantities(*updatedMetabolites)
    }
}
