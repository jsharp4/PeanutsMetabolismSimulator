package jon.sharp.metabolism.simulator.model

object PhysicalConstants {

    val AVOGADRO: Double = 6.022e23
    /**
     * Converts grams to millimoles using the provided molar mass.
     * @param grams The mass in grams
     * @param molarMass The molar mass in g/mol
     * @return The amount in millimoles
     */
    fun gramsToMillimoles(grams: Double, molarMass: Double): Double {
        return (grams / molarMass) * 1000
    }

    /**
     * Converts millimoles to grams using the provided molar mass.
     * @param millimoles The amount in millimoles
     * @param molarMass The molar mass in g/mol
     * @return The mass in grams
     */
    fun millimolesToGrams(millimoles: Double, molarMass: Double): Double {
        return (millimoles / 1000) * molarMass
    }

    fun milliMolesToMgDlPlasma(millimoles: Double, molarMass: Double): Double {
        val grams = millimolesToGrams(millimoles, molarMass)
        val milliGrams = grams * 1000
        val mgDl = milliGrams / (Blood.TOTAL_LITERS * 10)
        return mgDl
    }

    fun MgDlPlasmaMillimoles(mgDl: Double, molarMass: Double): Double {
        val milligrams = mgDl * (Blood.TOTAL_LITERS * 10)
        val grams = milligrams / 1000
        return gramsToMillimoles(grams, molarMass)
    }

    /**
     * Consolidated molar mass values for all molecules (in g/mol)
     */
    object MolarMass {
        val STARCH = 200000.0  // Average for starch polymer
        val ARACHIN_POLYPEPTIDE = 68000.0
        val SMALL_PEPTIDE_CHAIN = 680.0  // Unsourced
        val GLUTAMIC_ACID = 147.134
        val ATP = 1014.4
        val ARACHIN = 490000.0
        val MALTOSE = 342.30
        val GLUCOSE = 180.16
        val OLEIC_ACID = 282.5
        val TRIOLEIN = 885.4
    }

    object Maltose {
        object Hydrolysis {
            val MAX_HYDROLYSIS_RATE = 0.69
            val MICHAELIS_CONSTANT = 3.7
            val JEJUNUM_LENGTH_CM = 200.0
            val LUMEN_VOLUME_LITERS = 1.0
        }
    }

    object Insulin {

        fun mmolToInternationalUnitConc(mmol: Double): Double {
            val picoMols = mmol * 1e9
            val picoMolPerL = picoMols / Blood.TOTAL_LITERS
            val interationalUnits = picoMolPerL / 6.0
            return interationalUnits
        }

        fun internationalUnitConcToMmol(iUnits: Double): Double {
            val picoMolPerL = iUnits * 6.00
            val picoMol = picoMolPerL * Blood.TOTAL_LITERS
            val mMol = picoMol * 1e-9
            return mMol
        }
    }

    object Glucose {

        val FASTING_LEVEL_MMOL = 25.0
        object Glycolysis {
            // --- Activation Constants (k_a) ---
            val ACTIVATION_CONSTANT_PYRUVATE_KINASE = 6.56e-1 // Unit: mmol^2 / L^2

            // --- Equilibrium Constants (k_eq) ---
            val EQUILIBRIUM_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE = 1.78 // Unit: dimensionless
            val EQUILIBRIUM_CONSTANT_ENOLASE = 3.80e-1 // Unit: dimensionless
            val EQUILIBRIUM_CONSTANT_TRANSALDOLASE_TRANSKETOLASE_F6P = 9.84 // Unit: L / mmol
            val EQUILIBRIUM_CONSTANT_TRANSALDOLASE_TRANSKETOLASE_3PG = 1.01e-1 // Unit: L / mmol

            // --- Michaelis Constants (k_m) ---
            val MICHAELIS_CONSTANT_ALDOLASE = 1.77 // Unit: mmol / L
            val MICHAELIS_CONSTANT_GLUCOSE_TRANSPORTER = 6.60 // Unit: mmol / L
            val MICHAELIS_CONSTANT_GLUCOSE_6_PHOSPHATE_ISOMERASE = 2.41 // Unit: mmol / L
            val MICHAELIS_CONSTANT_GLUCOSE_6_PHOSPHATE_DEHYDROGENASE = 3.98 // Unit: mmol / L
            val MICHAELIS_CONSTANT_HEXOKINASE = 0.02 // Unit: mmol / L
            val MICHAELIS_CONSTANT_PHOSPHOFRUCTOKINASE = 1.08e-2 // Unit: mmol / L
            val MICHAELIS_CONSTANT_PYRUVATE_KINASE = 1.66e-3 // Unit: mmol / L
            val MICHAELIS_CONSTANT_URIDYLYLTRANSFERASE = 9.96e-3 // Unit: mmol / L

            // --- Maximum Enzymatic Velocities (v_max) ---
            val MAX_VELOCITY_ALDOLASE = 2.36e-11 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_GLUCOSE_TRANSPORTER = 1.60e-11 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_GLUCOSE_6_PHOSPHATE_DEHYDROGENASE = 5.81e-11 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_GLUCOSE_6_PHOSPHATE_ISOMERASE = 2.72e-10 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_HEXOKINASE = 1.92e-11 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_PHOSPHOFRUCTOKINASE = 1.00e-11 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_PYRUVATE_KINASE = 1.23e-9 // Unit: mmol / (cell * min)
            val MAX_VELOCITY_URIDYLYLTRANSFERASE = 8.17e-15 // Unit: mmol / (cell * min)

            // --- Specific Activities / Rate Constants (v) ---
            val ACTIVITY_ENOLASE = 2.34e-10 // Unit: mmol / (cell * min)
            val ACTIVITY_GLYCOGENESIS = 1.91e-14 // Unit: L / (cell * min)
            val ACTIVITY_REGULATION_PYRUVATE_KINASE = 3.69e-11 // Unit: L / (cell * min)
            val ACTIVITY_TRANSALDOLASE_TRANSKETOLASE_F6P = 3.73e-14 // Unit: L / (cell * min)
            val ACTIVITY_TRANSALDOLASE_TRANSKETOLASE_3PG = 5.60e-13 // Unit: L / (cell * min)
        }
    }

    object Blood {
        val TOTAL_LITERS = 5.0
        object FastingValues {
            val basalGlucoseMgDl = 90
            val basalInsulin = 7.5
            val basalBCAA = 475
        }
    }

    object Cells {
        val TOTAL_COUNT = 30.00e12
        val SINGLE_VOLUME = 3.04e-12

        val MITOCHONDRIA_PER_CELL = 1000

        val ACTIVE_ATP_SYNTHASE_PER_MITOCHONDRIA = 1000
    }

    object FattyAcids {
        object CarbonChainLength {
            val OLEIC_ACID = 18
        }
    }
}