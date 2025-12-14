package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.AbstractMetabolizer
import jon.sharp.metabolism.simulator.model.AbstractODESolver
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class Trypsin: AbstractMetabolizer(
    TrypsinODESolver(),
    listOf(
        MetaboliteType.POLYPEPTIDE,
        MetaboliteType.PEPTIDE_CHAIN
    )
) {

}

class TrypsinODESolver: AbstractODESolver() {
    val ode = TrypsinODE()
    override fun getODE() = ode
}

class TrypsinODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //polypeptide chain, small peptide chain
        yDot!![0] = -y!![0] * 0.2
        val yDot1Grams = PhysicalConstants.millimolesToGrams(
            y[0] * 0.2,
            PhysicalConstants.ARARCHIN_POLYPEPTIDE.MOLAR_MASS
        )
        yDot[1] = PhysicalConstants.gramsToMillimoles(
            yDot1Grams,
            PhysicalConstants.SMALL_PEPTIDE_CHAIN.MOLAR_MASS_UNSOURCED
        )
    }
}