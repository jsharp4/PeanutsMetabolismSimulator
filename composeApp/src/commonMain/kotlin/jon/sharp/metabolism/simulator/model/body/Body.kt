package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteMap

expect class Body {
    suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap)

    fun getOrganMetabolites(organName: String): MetaboliteMap

    fun getOrganNames(): List<String>
}