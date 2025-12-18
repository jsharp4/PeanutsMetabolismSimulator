package jon.sharp.metabolism.simulator.model

enum class MetaboliteType {
    STARCH,
    MALTOSE,
    GLUCOSE,
    CELL_GLUCOSE,
    G6P,
    F6P,
    F16BP,
    e3PG,
    PEP,
    PYRUVATE,
    ACETYL_COA,
    GTP,
    NADH,
    FADH2,
    ATP,
    ARACHIN,
    POLYPEPTIDE,
    PEPTIDE_CHAIN,
    GLUTAMIC_ACID,
    ALANINE,
    KETOGLUTARATE,
    BILE_SALT,
    TRIOLEIN,
    MAG,
    GLYCEROL,
    OLEIC_ACID,
    MICELLE,
    CHYLOMICRON,
    CHYLOMICRON_REMNANT,
    FATTY_ACYL_COA,
    INSULIN
}
data class Metabolite(
    val type: MetaboliteType,
    var amountMilliMoles: Float,
)