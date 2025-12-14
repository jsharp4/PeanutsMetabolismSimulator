package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.AbstractMetabolizer
import jon.sharp.metabolism.simulator.model.AbstractODESolver
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class Peptidase: AbstractMetabolizer(
    PeptidaseODESolver(),
    listOf(
        MetaboliteType.PEPTIDE_CHAIN,
        MetaboliteType.GLUTAMIC_ACID
    )
) {

}

class PeptidaseODESolver: AbstractODESolver() {
    val ode = PeptidaseODE()

    override fun getODE() = ode
}

class PeptidaseODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //peptide chain, glutamate
        yDot!![0] = -y!![0] * 0.2
        val yDot1Grams = PhysicalConstants.millimolesToGrams(
            y[0] * 0.2,
            PhysicalConstants.SMALL_PEPTIDE_CHAIN.MOLAR_MASS_UNSOURCED
        )
        yDot[1] = PhysicalConstants.gramsToMillimoles(
            yDot1Grams,
            PhysicalConstants.GLUTAMIC_ACID.MOLAR_MASS
        )
    }
}