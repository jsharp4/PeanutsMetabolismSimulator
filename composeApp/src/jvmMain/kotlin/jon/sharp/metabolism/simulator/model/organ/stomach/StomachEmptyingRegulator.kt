package jon.sharp.metabolism.simulator.model.organ.stomach

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import java.lang.Math.pow
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow

class StomachEmptyingRegulator: Metabolizer(
    StomachEmptyingODE(),
    listOf(
        MetaboliteType.STARCH,
        MetaboliteType.MALTOSE,
        MetaboliteType.POLYPEPTIDE,
        MetaboliteType.TRIOLEIN
    )
)

class StomachEmptyingODE: FirstOrderDifferentialEquations {

    private val RATE_CONSTANT = 0.0155
    private val LAG_CONSTANT = 2.62

    override fun getDimension() = 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {

        //avoid case with zero change at t = 0
        val clampedT = max(t, 0.01)

        val base = (1 - exp(-RATE_CONSTANT * clampedT))
        val yDotGeneral = -LAG_CONSTANT * base.pow(LAG_CONSTANT - 1) * RATE_CONSTANT * exp(-RATE_CONSTANT * clampedT)
        //assume that we pass everything at constant rates
        yDot!!.fill(yDotGeneral)
    }

}