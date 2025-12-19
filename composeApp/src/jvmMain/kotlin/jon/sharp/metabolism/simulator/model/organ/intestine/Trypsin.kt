package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class Trypsin: Metabolizer(
    ode = TrypsinODE(),
    metaboliteTypes = listOf(
        MetaboliteType.POLYPEPTIDE,
        MetaboliteType.PEPTIDE_CHAIN
    )
) {

}

class TrypsinODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //polypeptide chain, small peptide chain

        val linearReactionRateMmolPerMin = 1.1e-2

        yDot!![0] = -linearReactionRateMmolPerMin
        val yDot1Grams = PhysicalConstants.millimolesToGrams(
            -yDot[0],
            PhysicalConstants.MolarMass.ARACHIN_POLYPEPTIDE
        )
        val yDot1Millimoles = PhysicalConstants.gramsToMillimoles(
            yDot1Grams,
            PhysicalConstants.MolarMass.SMALL_PEPTIDE_CHAIN
        )
        yDot[1] = yDot1Millimoles
    }
}