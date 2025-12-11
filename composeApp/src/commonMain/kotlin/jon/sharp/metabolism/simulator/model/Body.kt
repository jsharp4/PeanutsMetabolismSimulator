package jon.sharp.metabolism.simulator.model

expect class Body {
    fun metabolizeTimeStep(initialInputs: MetaboliteMap)

    fun getOrganMetabolites(organName: String): MetaboliteMap

    fun getOrganNames(): List<String>
}
