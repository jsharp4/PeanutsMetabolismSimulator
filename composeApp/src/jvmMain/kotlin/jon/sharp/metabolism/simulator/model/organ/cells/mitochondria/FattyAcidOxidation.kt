package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.MetabolicProcess
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class FattyAcidOxidation: MetabolicProcess(
    ode = FattyAcidOxidationODE(),
    metaboliteTypes = listOf(
        MetaboliteType.FATTY_ACYL_COA,
        MetaboliteType.ACETYL_COA,
        MetaboliteType.NADH,
        MetaboliteType.FADH2
    )
)

class FattyAcidOxidationODE: FirstOrderDifferentialEquations {
    override fun getDimension(): Int {
        return 4
    }

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //fatty acylCoA, acetylCoA, NADH, FADH2
        yDot!![0] = -0.5 * y!![0]

        val numCleavages = PhysicalConstants.FattyAcids.CarbonChainLength.OLEIC_ACID / 2 - 1

        yDot[1] = -yDot[0] * numCleavages
        yDot[2] = yDot[1]
        yDot[3] = yDot[2]
    }
}