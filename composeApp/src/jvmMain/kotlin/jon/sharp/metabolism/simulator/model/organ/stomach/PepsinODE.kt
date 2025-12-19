package jon.sharp.metabolism.simulator.model.organ.stomach

import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import kotlin.math.exp

class PepsinODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //ARACHIN, POLYPEPTIDE

        val hydrolyzedPercentage120Min = 87.0

        val reactionRateConstantMinutes = 0.08

        val dHydrolyzedPercentageDt = reactionRateConstantMinutes *
                hydrolyzedPercentage120Min *
                exp(-reactionRateConstantMinutes * t)

        val assumedAmountAlreadyHydrolyzed = hydrolyzedPercentage120Min *
                (1 - exp(-reactionRateConstantMinutes * t))

        val assumedOriginalProteinCount = y!![0] / (1 - assumedAmountAlreadyHydrolyzed / 100)

        val newPercentHyrolyzed = assumedAmountAlreadyHydrolyzed + dHydrolyzedPercentageDt

        val newProteinCount = assumedOriginalProteinCount * (1 - newPercentHyrolyzed / 100)

        yDot!![0] = newProteinCount - y[0]
        val yDot1Grams = PhysicalConstants.millimolesToGrams(
            -yDot[0],
            PhysicalConstants.MolarMass.ARACHIN
        )
        val yDotPolypeptideMilliMoles = PhysicalConstants.gramsToMillimoles(
            yDot1Grams,
            PhysicalConstants.MolarMass.ARACHIN_POLYPEPTIDE
        )

        yDot[1] = yDotPolypeptideMilliMoles
    }

}