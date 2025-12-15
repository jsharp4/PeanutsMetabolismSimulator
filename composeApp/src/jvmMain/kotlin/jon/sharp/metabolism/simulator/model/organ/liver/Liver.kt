package jon.sharp.metabolism.simulator.model.organ.liver

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.ODESolver
import jon.sharp.metabolism.simulator.model.Organ
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class Liver: Organ(
    setOf(
        BileSaltProducer()
    )
)

class BileSaltProducer: Metabolizer(
    ODESolver(LiverBileSaltODE()),
    listOf(
        MetaboliteType.BILE_SALT
    )
)

class LiverBileSaltODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 1

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        yDot!![0] = 1.0
    }

}