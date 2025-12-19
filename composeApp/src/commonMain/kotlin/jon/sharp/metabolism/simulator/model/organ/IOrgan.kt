package jon.sharp.metabolism.simulator.model.organ

import jon.sharp.metabolism.simulator.model.MetaboliteMap

interface IOrgan {
    fun metabolizeTimeStep(inputs: MetaboliteMap = MetaboliteMap(), t0: Double)

    fun getMetabolites(): MetaboliteMap

    fun getName(): String
}