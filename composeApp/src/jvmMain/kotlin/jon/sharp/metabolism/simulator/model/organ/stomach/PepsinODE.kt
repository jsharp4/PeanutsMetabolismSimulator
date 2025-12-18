package jon.sharp.metabolism.simulator.model.organ.cells.stomach

import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class PepsinODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //GLUTAMATE, POLYPEPTIDE
        yDot!![0] = -y!![0] * 0.2
        val yDot1Grams = PhysicalConstants.millimolesToGrams(
            y[0] * 0.2,
            PhysicalConstants.MolarMass.ARACHIN
        )
        yDot[1] = PhysicalConstants.gramsToMillimoles(
            yDot1Grams,
            PhysicalConstants.MolarMass.ARARCHIN_POLYPEPTIDE
        )
    }

}