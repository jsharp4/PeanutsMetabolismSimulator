package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.AbstractODESolver
import jon.sharp.metabolism.simulator.model.ODESolver
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class TcaODESolver: AbstractODESolver() {
    val ode = TcaODE()
    override fun getODE() = ode
}

class TcaODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //Pyruvate (for now), NADH, FADH2, GTP
        val usedPyruvate = y!![0] * 0.75

        yDot!![0] = -usedPyruvate
        yDot[1] = 3 * usedPyruvate
        yDot[2] = usedPyruvate
        yDot[3] = usedPyruvate
    }
}