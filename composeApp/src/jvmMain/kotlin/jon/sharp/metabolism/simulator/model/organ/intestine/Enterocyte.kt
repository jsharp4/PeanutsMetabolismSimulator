package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.MetabolicProcess
import jon.sharp.metabolism.simulator.model.Organ
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class Enterocytes: Organ(
    listOf(
        Golgi()
    )
)

class Golgi: MetabolicProcess(
    ode = ChylomicronProducerODE(),
    metaboliteTypes = listOf(
        MetaboliteType.MICELLE,
        MetaboliteType.CHYLOMICRON
    )
)

class ChylomicronProducerODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //mixed micelles, chylomicrons
        yDot!![0] = -0.5 * y!![0]
        yDot[1] = -yDot[0]
    }

}