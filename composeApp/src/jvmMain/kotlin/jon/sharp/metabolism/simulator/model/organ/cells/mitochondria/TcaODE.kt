package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class TcaODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 5

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //Pyruvate (for now), NADH, FADH2, GTP
        val usedPyruvate = y!![0] * 0.75
        val usedKetoglutarate = y[4] * 0.75

        yDot!![0] = -usedPyruvate
        yDot[4] = -usedKetoglutarate
        yDot[1] = 3 * usedPyruvate + 2 * usedKetoglutarate
        yDot[2] = usedPyruvate + usedKetoglutarate
        yDot[3] = usedPyruvate + usedKetoglutarate
    }
}