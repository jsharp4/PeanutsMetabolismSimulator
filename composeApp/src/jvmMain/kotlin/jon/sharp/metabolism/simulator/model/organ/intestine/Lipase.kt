package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.MetabolicProcess
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import kotlin.math.exp

class Lipase: MetabolicProcess(
    ode = LipaseODE(),
    metaboliteTypes = listOf(
        MetaboliteType.TRIOLEIN,
        MetaboliteType.DAG,
        MetaboliteType.MAG,
        MetaboliteType.OLEIC_ACID
    )
) {
}

class LipaseODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        // triolein, DAG, MAG, oleic acid
        val rateConstantTrio = 0.0276
        val rateConstantDio = 0.2430

        val dTrioleinDt = -rateConstantTrio * y!![0] * exp(-rateConstantTrio * t)

        val dDioDt = y[0] * rateConstantTrio / (rateConstantTrio - rateConstantDio) *
                (-rateConstantTrio * exp(-rateConstantTrio * t) -
                        -rateConstantDio * exp(-rateConstantDio * t))

        val dMonoDt = y[0] / (rateConstantDio - rateConstantTrio) *
                ((rateConstantTrio * rateConstantDio) * (-exp(-rateConstantTrio * t) + exp(-rateConstantDio * t)))

        val dOleicDt = dTrioleinDt + dDioDt

        yDot!![0] = dTrioleinDt
        yDot[1] = dDioDt
        yDot[2] = dMonoDt
        yDot[3] = dOleicDt
    }
}