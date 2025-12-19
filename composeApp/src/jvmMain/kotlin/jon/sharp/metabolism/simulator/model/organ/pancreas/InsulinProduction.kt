package jon.sharp.metabolism.simulator.model.organ.pancreas

import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.organ.blood.Blood
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations

class InsulinProduction: Metabolizer(
    InsulinODE(),
    listOf(
        MetaboliteType.GLUCOSE,
        MetaboliteType.GLUTAMIC_ACID,
        MetaboliteType.INSULIN
    )
)

class InsulinODE: FirstOrderDifferentialEquations {
    override fun getDimension() = 3

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {
        val kGlucose = 0.0974
        val kAminoAcid = 0.0247
        val kInsulin = 0.14

        val basalGlucoseMgDl = 90
        val basalInsulin = 7.5
        val basalBCAA = 475

        val currGlucoseMdDl = PhysicalConstants.milliMolesToMgDlPlasma(y!![0], PhysicalConstants.MolarMass.GLUCOSE)
        val currAminoAcidMmolPerL = y[1] / PhysicalConstants.Blood.TOTAL_LITERS
        val currInsulin = PhysicalConstants.Insulin.mmolToInternationalUnitConc(y[2])

        yDot!![0] = 0.0
        yDot[1] = 0.0

        val steadyStateFactor = -kGlucose * basalGlucoseMgDl
                                    - kAminoAcid * basalBCAA
                                    + kInsulin * basalInsulin

        val glucoseFactor = kGlucose * y!![0]

        //base blood AA levels are not regulated in the simulation, so we assume a typical base
        // value in the calculation here
        val aminAcidFactor = kAminoAcid * (basalBCAA + y[1])

        //base blood insulin levels are also not tracked
        val insulinFactor = -kInsulin + (basalInsulin + y[2])

        val dInsulinDtInternationalUnits = glucoseFactor + aminAcidFactor + insulinFactor + steadyStateFactor
        val dInsulinDtMmol = PhysicalConstants.Insulin.internationalUnitConcToMmol(dInsulinDtInternationalUnits)
        yDot[2] = dInsulinDtMmol
    }

}