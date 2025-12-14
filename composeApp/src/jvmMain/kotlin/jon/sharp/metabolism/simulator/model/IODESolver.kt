package jon.sharp.metabolism.simulator.model

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

/**
 * Interface for ODE solvers used in the metabolism simulation.
 * Implementations integrate differential equations over time to simulate metabolic processes.
 */
interface IODESolver {
    /**
     * Gets the differential equations being solved by this solver.
     */
    fun getODE(): FirstOrderDifferentialEquations

    /**
     * Steps the system forward by one minute (1.0 time unit).
     *
     * @param state The current state vector
     * @param startTime The starting time (default 0.0)
     * @return The state vector after one minute of simulation
     */
    fun stepForwardOneMinute(state: DoubleArray, startTime: Double = 0.0): DoubleArray
}
