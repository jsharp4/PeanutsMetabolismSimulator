package jon.sharp.metabolism.simulator.model.organ.cells.mitochondria

import jon.sharp.metabolism.simulator.model.MetabolicProcess
import jon.sharp.metabolism.simulator.model.MetaboliteType
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class AlanineAminoTransferase: MetabolicProcess(
    ode = AminoTransferaseODE(),
    metaboliteTypes = listOf(
        MetaboliteType.GLUTAMIC_ACID,
        MetaboliteType.PYRUVATE,
        MetaboliteType.KETOGLUTARATE,
        MetaboliteType.ALANINE
    )
)

class AminoTransferaseODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //glutamate, pyruvate, ketoglutarate, alanine
        val conversionRate = 0.3
        yDot!![0] = -conversionRate * y!![0]
        yDot!![1] = -conversionRate * y!![0]
        yDot!![2] = conversionRate * y!![0]
        yDot!![3] = conversionRate * y!![0]
    }
}

