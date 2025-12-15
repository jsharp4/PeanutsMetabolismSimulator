package jon.sharp.metabolism.simulator.model.body

import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.organ.IOrgan

expect class Body {

    val organNameMap: Map<String, IOrgan>
    suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap)

//    fun getOrganMetabolites(organName: String): MetaboliteMap
//
//    fun getOrganNames(): List<String>

    fun getTransportGraph(): BodyTransportGraph
}