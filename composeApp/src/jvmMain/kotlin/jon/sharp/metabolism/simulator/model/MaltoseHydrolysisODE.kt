package jon.sharp.metabolism.simulator.model

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import kotlin.math.max

/**
 * ODE for maltose hydrolysis to glucose in the intestinal lumen.
 *
 * State vector (all in mmol):
 * [0] = Maltose mass
 * [1] = Glucose mass
 *
 * Reaction: Maltose → 2 Glucose
 */
class MaltoseHydrolysisODE(
    val maxHydrolysisRate: Double,
    val lumenVolume: Double,
    val jejenumSegmentLength: Double,
    val machaelisConstant: Double,
): FirstOrderDifferentialEquations {
    private val k = maxHydrolysisRate * jejenumSegmentLength

    override fun getDimension(): Int {
        return 2
    }

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        val maltose = max(0.0, y!![0])

        // Michaelis-Menten kinetics for maltose consumption
        val hydrolysisRate = k * (maltose / (machaelisConstant * lumenVolume + maltose))

        // d[Maltose]/dt - consumed by hydrolysis
        yDot!![0] = -hydrolysisRate

        // d[Glucose]/dt - produced by hydrolysis (2 glucose per maltose)
        yDot[1] = 2.0 * hydrolysisRate
    }
}