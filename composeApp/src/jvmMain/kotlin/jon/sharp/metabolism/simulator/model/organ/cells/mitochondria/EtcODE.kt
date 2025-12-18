package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.PhysicalConstants.AVOGADRO
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class EtcODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 3

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
         //NADH, FADH2, ATP
        // ~100 ATP per second, per cell (strong assumption one on synthase per cell)
        // 2.5 from NADH, 1.5 from FADH2

        val atpPerNADH = 2.5
        val atpPerFADH2 = 1.5

        val atpMoleculesPerMinPerSynthase = 100 * 60.0
        val atpMoleculesPerMinuteTotal =
            atpMoleculesPerMinPerSynthase *
                    PhysicalConstants.Cells.TOTAL_COUNT *
                    PhysicalConstants.Cells.MITOCHONDRIA_PER_CELL *
                    PhysicalConstants.Cells.ACTIVE_ATP_SYNTHASE_PER_MITOCHONDRIA
        val atpMilliMolsPerMinuteTotal = atpMoleculesPerMinuteTotal / AVOGADRO * 1000

        //assumption for simplicity that they can each only use a fixed amount
        val percentUsableByNADH = atpPerNADH / (atpMoleculesPerMinuteTotal + atpPerFADH2)
        val percentUsableByFADH2 = atpPerFADH2 / (atpMoleculesPerMinuteTotal + atpPerFADH2)

        val maxAtpFromNADH = atpMilliMolsPerMinuteTotal * percentUsableByNADH
        val potentiallyUsedNADH = maxAtpFromNADH / atpPerNADH
        val actuallyUsedNADH = kotlin.math.min(potentiallyUsedNADH, y!![0])

        val maxAtpFromFADH2 = atpMilliMolsPerMinuteTotal * percentUsableByFADH2
        val potentiallyUsedFADH2 = maxAtpFromFADH2 / atpPerFADH2
        val actuallyUsedFADH2 = kotlin.math.min(potentiallyUsedFADH2, y[1])

        yDot!![0] = -actuallyUsedNADH

        yDot[1] = -actuallyUsedFADH2

        yDot[2] = actuallyUsedNADH * atpPerNADH + actuallyUsedFADH2 * atpPerFADH2

    }
}