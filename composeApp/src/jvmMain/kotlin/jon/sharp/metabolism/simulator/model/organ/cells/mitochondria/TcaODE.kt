package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class TcaODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 6

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //Pyruvate (for now), NADH, FADH2, GTP, ketoglutarate, ACetylCoA
        val usedPyruvate = y!![0] * 0.75
        val usedKetoglutarate = y[4] * 0.75
        val usedAcetylCoA = y[5] * 0.75

        yDot!![0] = -usedPyruvate
        yDot[4] = -usedKetoglutarate
        yDot[5] = -usedAcetylCoA
        yDot[1] = 3 * usedPyruvate + 2 * usedKetoglutarate + 3 * usedAcetylCoA
        yDot[2] = usedPyruvate + usedKetoglutarate + usedAcetylCoA
        yDot[3] = usedPyruvate + usedKetoglutarate + usedAcetylCoA
    }
}