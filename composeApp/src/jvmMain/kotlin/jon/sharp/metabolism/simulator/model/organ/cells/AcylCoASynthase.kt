package jon.sharp.metabolism.simulator.model.organ.cells

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.MetabolicProcess
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class AcylCoASynthase: MetabolicProcess(
    ode = AcylCoASynthaseODE(),
    metaboliteTypes = listOf(
        MetaboliteType.OLEIC_ACID,
        MetaboliteType.FATTY_ACYL_COA
    )
)

class AcylCoASynthaseODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //OLEIC_ACID, FattyAcylCoA
        yDot!![0] = -0.5 * y!![0]
        yDot[1] = -yDot[0]
    }

}