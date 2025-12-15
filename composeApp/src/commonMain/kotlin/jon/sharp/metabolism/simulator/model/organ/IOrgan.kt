package jon.sharp.metabolism.simulator.model.organ

import jon.sharp.metabolism.simulator.model.MetaboliteMap

interface IOrgan {
    fun metabolizeTimeStep(inputs: MetaboliteMap = MetaboliteMap())

    fun getMetabolites(): MetaboliteMap

    fun getName(): String
}