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
    GTP,
    NADH,
    FADH2,
    ATP
}
data class Metabolite(
    val type: MetaboliteType,
    var amountMilliMoles: Float,
)