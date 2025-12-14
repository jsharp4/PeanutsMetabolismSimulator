package jon.sharp.metabolism.simulator.model

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator
import kotlin.math.max

/**
 * Concrete ODE solver that provides common integration logic.
 *
 * This class handles:
 * - Integrator initialization with standard parameters
 * - Common integration workflow via stepForwardOneMinute
 * - Ensuring non-negative values after integration
 *
 * @param ode The differential equations to solve
 */
open class ODESolver(
    private val ode: FirstOrderDifferentialEquations
) : IODESolver {

    /**
     * The integrator used for solving the differential equations.
     * Uses Dormand-Prince 8(5,3) method with adaptive step size.
     */
    protected val integrator = DormandPrince853Integrator(
        1e-6,   // minStep
        1.0,    // maxStep
        1e-10,  // absoluteTolerance
        1e-10   // relativeTolerance
    )

    /**
     * Returns the differential equations being solved by this solver.
     */
    override fun getODE() = ode

    /**
     * Steps the system forward by one minute using the ODE integrator.
     * This implementation clamps all negative values to 0.0 after integration.
     *
     * @param state The current state vector
     * @param startTime The starting time (default 0.0)
     * @return The state vector after one minute of simulation (all values >= 0)
     */
    override fun stepForwardOneMinute(state: DoubleArray, startTime: Double): DoubleArray {
        return integrate(state, startTime, startTime + 1.0, clampNegatives = true)
    }

    /**
     * Performs integration from t0 to t1 and optionally ensures all values are non-negative.
     *
     * @param initialState The initial state vector
     * @param startTime The starting time
     * @param endTime The ending time (typically startTime + 1.0 for one minute)
     * @param clampNegatives If true, clamps all negative values to 0.0 after integration
     * @return The final state vector after integration
     */
    protected fun integrate(
        initialState: DoubleArray,
        startTime: Double,
        endTime: Double,
        clampNegatives: Boolean = true
    ): DoubleArray {
        val finalState = initialState.copyOf()
        integrator.integrate(getODE(), startTime, initialState, endTime, finalState)

        return if (clampNegatives) {
            finalState.map { max(0.0, it) }.toDoubleArray()
        } else {
            finalState
        }
    }
}
