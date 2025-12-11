package jon.sharp.metabolism.simulator.model

expect class Body {
    suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap)

    fun getOrganMetabolites(organName: String): MetaboliteMap

    fun getOrganNames(): List<String>
}
