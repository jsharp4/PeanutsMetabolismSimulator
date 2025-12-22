package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.MetabolicProcess
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import kotlin.math.min

class MixedMicelleFormer: MetabolicProcess(
    ode = MixedMicelleFormerODE(),
    metaboliteTypes = listOf(
        MetaboliteType.MAG,
        MetaboliteType.OLEIC_ACID,
        MetaboliteType.BILE_SALT,
        MetaboliteType.MICELLE
    )
)

class MixedMicelleFormerODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //MAG, OLEIC_ACID, BILE_SALTS, MICELLE
        val limitingSubstrate = min(y!![0], min(y[1], y[2]))
        val rateOfChange = 0.5 * limitingSubstrate
        yDot!![0] = -rateOfChange
        yDot[1] = -2 * rateOfChange
        yDot[2] = -rateOfChange
        yDot[3] = rateOfChange
    }
}