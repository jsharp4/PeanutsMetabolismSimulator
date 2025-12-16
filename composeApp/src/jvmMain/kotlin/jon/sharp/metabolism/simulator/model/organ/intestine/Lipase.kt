package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.ODESolver
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class Lipase: Metabolizer(
    ODESolver(LipaseODE()),
    listOf(
        MetaboliteType.TRIOLEIN,
        MetaboliteType.OLEIC_ACID,
        MetaboliteType.GLYCEROL
    )
) {
}

class LipaseODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 3

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        // triolein, oleic acid, glycerol
        yDot!![0] = -0.1 * y!![0]

        yDot[1] = 3 * yDot[0] * -1

        yDot[2] = yDot[0] * -1
    }
}