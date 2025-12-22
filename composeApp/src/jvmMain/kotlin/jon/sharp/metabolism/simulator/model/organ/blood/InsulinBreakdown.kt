package jon.sharp.metabolism.simulator.model.organ.blood

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.MetabolicProcess
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import kotlin.math.ln

class InsulinBreakdown: MetabolicProcess(
    InsulinBreakdownODE(),
    listOf(
        MetaboliteType.INSULIN
    )
) {
}

class InsulinBreakdownODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 1

    val rateConstant = ln(2.0) / 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        yDot!![0] = y!![0] * -rateConstant
    }
}