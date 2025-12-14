package jon.sharp.metabolism.simulator.model.organ.cells

import jon.sharp.metabolism.simulator.model.PhysicalConstants
import kotlin.math.max
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.ACTIVATION_CONSTANT_PYRUVATE_KINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.ACTIVITY_ENOLASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.ACTIVITY_TRANSALDOLASE_TRANSKETOLASE_3PG
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.ACTIVITY_TRANSALDOLASE_TRANSKETOLASE_F6P
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.EQUILIBRIUM_CONSTANT_ENOLASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.EQUILIBRIUM_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.EQUILIBRIUM_CONSTANT_TRANSALDOLASE_TRANSKETOLASE_3PG
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.EQUILIBRIUM_CONSTANT_TRANSALDOLASE_TRANSKETOLASE_F6P
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_ALDOLASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_GLUCOSE_6_PHOSPHATE_DEHYDROGENASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_GLUCOSE_6_PHOSPHATE_ISOMERASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_GLUCOSE_TRANSPORTER
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_HEXOKINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_PHOSPHOFRUCTOKINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_PYRUVATE_KINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MAX_VELOCITY_URIDYLYLTRANSFERASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_ALDOLASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_GLUCOSE_6_PHOSPHATE_DEHYDROGENASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_GLUCOSE_TRANSPORTER
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_HEXOKINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_PHOSPHOFRUCTOKINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_PYRUVATE_KINASE
import jon.sharp.metabolism.simulator.model.PhysicalConstants.Glucose.Glycolysis.MICHAELIS_CONSTANT_URIDYLYLTRANSFERASE
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator

class GlycolysisODESolver : jon.sharp.metabolism.simulator.model.AbstractODESolver(GlycolysisODE()) {

    /**
     * Conversion factor for cellular metabolites: total cellular volume in liters.
     * Used to convert between mass (mmol) and concentration (mmol/L) for cellular metabolites.
     */
    private val totalCellularVolume = PhysicalConstants.Cells.TOTAL_COUNT * PhysicalConstants.Cells.SINGLE_VOLUME

    /**
     * Steps forward one minute given the current metabolite masses.
     *
     * State vector (all values in mmol):
     * [0] = GLCx (Extracellular/Blood Glucose)
     * [1] = GLC (Intracellular Glucose)
     * [2] = G6P (Glucose-6-Phosphate)
     * [3] = F6P (Fructose-6-Phosphate)
     * [4] = F16BP (Fructose-1,6-Bisphosphate)
     * [5] = 3PG (3-Phosphoglycerate)
     * [6] = PEP (Phosphoenolpyruvate)
     * [7] = Pyruvate
     *
     * @param state The current state vector with masses in mmol
     * @param startTime The starting time (default 0.0)
     * @return The state vector after one minute with masses in mmol
     */
    override fun stepForwardOneMinute(state: DoubleArray, startTime: Double): DoubleArray {
        // Convert masses to concentrations for the ODE
        val concentrations = massesToConcentrations(state)

        // Integrate using concentrations
        val finalConcentrations = super.stepForwardOneMinute(concentrations, startTime)

        // Convert back to masses
        return concentrationsToMasses(finalConcentrations)
    }

    /**
     * Converts masses (mmol) to concentrations (mmol/L).
     * - Blood glucose: divide by blood volume
     * - Cellular metabolites: divide by total cellular volume
     */
    private fun massesToConcentrations(masses: DoubleArray): DoubleArray {
        return doubleArrayOf(
            masses[0] / PhysicalConstants.Blood.TOTAL_LITERS,  // Blood glucose
            masses[1] / totalCellularVolume,                   // Intracellular glucose
            masses[2] / totalCellularVolume,                   // G6P
            masses[3] / totalCellularVolume,                   // F6P
            masses[4] / totalCellularVolume,                   // F16BP
            masses[5] / totalCellularVolume,                   // 3PG
            masses[6] / totalCellularVolume,                   // PEP
            masses[7] / totalCellularVolume                    // Pyruvate
        )
    }

    /**
     * Converts concentrations (mmol/L) to masses (mmol).
     * - Blood glucose: multiply by blood volume
     * - Cellular metabolites: multiply by total cellular volume
     */
    private fun concentrationsToMasses(concentrations: DoubleArray): DoubleArray {
        return doubleArrayOf(
            concentrations[0] * PhysicalConstants.Blood.TOTAL_LITERS,  // Blood glucose
            concentrations[1] * totalCellularVolume,                   // Intracellular glucose
            concentrations[2] * totalCellularVolume,                   // G6P
            concentrations[3] * totalCellularVolume,                   // F6P
            concentrations[4] * totalCellularVolume,                   // F16BP
            concentrations[5] * totalCellularVolume,                   // 3PG
            concentrations[6] * totalCellularVolume,                   // PEP
            concentrations[7] * totalCellularVolume                    // Pyruvate
        )
    }
}

class GlycolysisODE: FirstOrderDifferentialEquations {

    override fun getDimension(): Int = 8

    override fun computeDerivatives(t: Double, y: DoubleArray?, yDot: DoubleArray?) {

        val yClamped = y!!.map { it -> max(it, 0.0) }.toDoubleArray()

        // keep F16BP above zero to prevent complete loss of feed-forward regulation
        yClamped[4] = max(yClamped[4], 0.0001)


        // --- Simulation Constants (from Table 1) ---
        // Cell Specific Volume (V_c_s)
        // Value from Lim1/Lim2 in Table 1 is approx 3.0e-12 L/cell
        val VOLUME_CELL_SPECIFIC = PhysicalConstants.Cells.SINGLE_VOLUME

        // Enzyme Level (E_level)
        // Scaling factor for batch-to-batch variation (usually ~1.0)
        val E_LEVEL = 1.0

        val TOTAL_CELLS = PhysicalConstants.Cells.TOTAL_COUNT

        val volRatio = (VOLUME_CELL_SPECIFIC * TOTAL_CELLS) / PhysicalConstants.Blood.TOTAL_LITERS


        // --- Helper Function for Effective Activity ---
        // Implements Eq (4): K_max = (v_max * E_level) / V_c_s
        fun getEffectiveActivity(vMaxPerCell: Double): Double {
            return (vMaxPerCell * E_LEVEL) / VOLUME_CELL_SPECIFIC
        }


        // --- Flux Calculations (Updated) ---

        // 1. Convert v_max (per cell) to K_max (per Volume)
        val kMaxGlut = getEffectiveActivity(MAX_VELOCITY_GLUCOSE_TRANSPORTER)
        val kMaxHk   = getEffectiveActivity(MAX_VELOCITY_HEXOKINASE)
        val kMaxGpi  = getEffectiveActivity(MAX_VELOCITY_GLUCOSE_6_PHOSPHATE_ISOMERASE)
        val kMaxG6pdh= getEffectiveActivity(MAX_VELOCITY_GLUCOSE_6_PHOSPHATE_DEHYDROGENASE)
        val kMaxUt   = getEffectiveActivity(MAX_VELOCITY_URIDYLYLTRANSFERASE)
        val kMaxPfk  = getEffectiveActivity(MAX_VELOCITY_PHOSPHOFRUCTOKINASE)
        val kMaxAld  = getEffectiveActivity(MAX_VELOCITY_ALDOLASE)
        val kMaxPk   = getEffectiveActivity(MAX_VELOCITY_PYRUVATE_KINASE)

        // Also convert linear activities (v) to (K)
        val kTatkF6p = getEffectiveActivity(ACTIVITY_TRANSALDOLASE_TRANSKETOLASE_F6P)
        val kTatk3pg = getEffectiveActivity(ACTIVITY_TRANSALDOLASE_TRANSKETOLASE_3PG)
        val kEno     = getEffectiveActivity(ACTIVITY_ENOLASE)


        // 2. Calculate Fluxes using the new K_max values

        // Glucose Transporter (GLUT)
        // Note: GLUT has special handling for total biomass in the influx equation,
        // but for internal consistency in concentration units, we use K_max logic.
        val vGlut = kMaxGlut * yClamped[0] / (MICHAELIS_CONSTANT_GLUCOSE_TRANSPORTER + yClamped[0])

        // Hexokinase (HK)
        val vHk = kMaxHk * yClamped[1] / (MICHAELIS_CONSTANT_HEXOKINASE + yClamped[1])

        // GPI
        val gpiNumerator = yClamped[2] - (yClamped[3] / EQUILIBRIUM_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE)
        val gpiDenominator = MICHAELIS_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE + yClamped[2] + (yClamped[3] / EQUILIBRIUM_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE)
        val vGpi = kMaxGpi * gpiNumerator / gpiDenominator

        // G6PDH
        val vG6pdh = kMaxG6pdh * yClamped[2] / (MICHAELIS_CONSTANT_GLUCOSE_6_PHOSPHATE_DEHYDROGENASE + yClamped[2])

        // UT
        val vUt = kMaxUt * yClamped[2] / (MICHAELIS_CONSTANT_URIDYLYLTRANSFERASE + yClamped[2])

        // PFK
        val f6pPow4 = Math.pow(yClamped[3], 4.0)
        val kmPfkPow4 = Math.pow(MICHAELIS_CONSTANT_PHOSPHOFRUCTOKINASE, 4.0)
        val vPfk = kMaxPfk * f6pPow4 / (kmPfkPow4 + f6pPow4)

        // ALD
        val vAld = kMaxAld * yClamped[4] / (MICHAELIS_CONSTANT_ALDOLASE + yClamped[4])

        // TATK
        val vTatkF6p = 0    //kTatkF6p * (1.0 - (yClamped[3] / EQUILIBRIUM_CONSTANT_TRANSALDOLASE_TRANSKETOLASE_F6P))
        val vTatk3pg = 0    //kTatk3pg * (1.0 - (yClamped[5] / EQUILIBRIUM_CONSTANT_TRANSALDOLASE_TRANSKETOLASE_3PG))

        // ENO
        val vEno = kEno * (yClamped[5] - (yClamped[6] / EQUILIBRIUM_CONSTANT_ENOLASE))

        // PK - OLD FORMULA (INHIBITION BUG):
        // val pkDenominator = MICHAELIS_CONSTANT_PYRUVATE_KINASE + yClamped[6] + (ACTIVATION_CONSTANT_PYRUVATE_KINASE / yClamped[4])
        // val vPk = kMaxPk * yClamped[6] / pkDenominator

        // PK - FIXED: Allosteric activation by F16BP
        // F16BP activates pyruvate kinase by reducing the effective Km
        // Using Ka units (mmol²/L²), the activation term uses [F16BP]²
        val f16bpSquared = yClamped[4] * yClamped[4]
        val activationFactor = 1.0 + (f16bpSquared / ACTIVATION_CONSTANT_PYRUVATE_KINASE)
        val effectiveKm = MICHAELIS_CONSTANT_PYRUVATE_KINASE / activationFactor
        val vPk = kMaxPk * yClamped[6] / (effectiveKm + yClamped[6])

        // Diagnostic logging for PEP issue
        if (t == 0.0 || (t % 1.0 < 0.01)) {
            println("t=$t: 3PG=${yClamped[5]}, PEP=${yClamped[6]}, F16BP=${yClamped[4]}")
            println("  vEno=$vEno, vPk=$vPk, d[PEP]/dt=${vEno - vPk}")
            println("  activationFactor=$activationFactor, effectiveKm=$effectiveKm (base Km=${MICHAELIS_CONSTANT_PYRUVATE_KINASE})")
        }

        // --- 4. Differential Equations (d/dt) ---

        // d[GLCx]/dt : Extracellular Glucose
        // Depends on biomass (Total Cells) and Medium Volume
        yDot!![0] = -vGlut * volRatio

        // d[GLC]/dt : Intracellular Glucose
        yDot[1] = vGlut - vHk

        // d[G6P]/dt
        yDot[2] = vHk - vGpi - vG6pdh - vUt

        // d[F6P]/dt
        yDot[3] = vGpi - vPfk + vTatkF6p

        // d[F16BP]/dt
        yDot[4] = vPfk - vAld

        // d[3PG]/dt
        // Note: ALD produces 2 molecules of triose phosphates
        yDot[5] = (2.0 * vAld) + vTatk3pg - vEno

        // d[PEP]/dt
        yDot[6] = vEno - vPk

        yDot[7] = vPk
    }
}