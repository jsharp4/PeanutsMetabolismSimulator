package jon.sharp.metabolism.simulator.model.organ.pancreas

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class InsulinProduction: Metabolizer(
    InsulinODE(),
    listOf(
        MetaboliteType.INSULIN
    )
)

class InsulinODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 1

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        yDot!![0] = 1.0 * t
    }

}