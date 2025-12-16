package jon.sharp.metabolism.simulator.model.organ.blood

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.ODESolver
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class LipoproteinLipase: Metabolizer(
    ode = LipoproteinLipaseODE(),
    metaboliteTypes = listOf(
        MetaboliteType.CHYLOMICRON,
        MetaboliteType.CHYLOMICRON_REMNANT,
        MetaboliteType.OLEIC_ACID,
        MetaboliteType.GLYCEROL
    )
)

class LipoproteinLipaseODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 4

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        //chylomicrons, chylomicron remnants, OLEIC_ACID, glycerol
        yDot!![0] = -0.1 * y!![0]

        yDot[1] = -yDot[0]

        yDot[2] = 10 * yDot[1]

        yDot[3] = yDot[2] / 3
    }
}