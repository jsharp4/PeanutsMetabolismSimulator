package jon.sharp.metabolism.simulator.model.organ.blood

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import kotlin.math.pow

class GlucoseUptakeRegulator: Metabolizer(
    GlucoseUptakeODE(),
    listOf(
        MetaboliteType.INSULIN,
        MetaboliteType.GLUCOSE
    )
)

class GlucoseUptakeODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 2

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        /**
         * Parameter values from Bizzotto et al. (2016)
         * "Glucose uptake saturation explains glucose kinetics profiles measured by different tests"
         */

        // Michaelis constant for glucose
        val kmG_mmol_l = 3.88

        // Basal maximum glucose uptake rate
        val vMax0_mmol_min_m2 = 0.338

        // Maximum effect of insulin on Vmax
        val eMax_mmol_min_m2 = 4.81

        // Insulin concentration for half-maximal effect
        val kmI_pmol_l = 784.0

        // Shape factor for the insulin effect (gamma)
        val gamma_dimensionless = 1.62

        // Glucose distribution volume

        val distributionVolume = 12.7
        val subjectBsaMetersSquared = 1.73

        val glucoseMmolPerLiter = y!![1] / distributionVolume

        val insulinPmolPerLiter = y[0] * 1e9 / distributionVolume

        val vMaxUpdate = vMax0_mmol_min_m2 +
                (eMax_mmol_min_m2 * insulinPmolPerLiter.pow(gamma_dimensionless)) /
                (kmI_pmol_l.pow(gamma_dimensionless) + insulinPmolPerLiter.pow(gamma_dimensionless))

        val dGlucoseConcDt = vMaxUpdate * glucoseMmolPerLiter /
                (kmG_mmol_l + glucoseMmolPerLiter)

        yDot!![0] = 0.0
        //kinetics model is for uptake in tissues. We take the negative rate to track loss from blood
        yDot[1] = -dGlucoseConcDt * subjectBsaMetersSquared
    }

}