package jon.sharp.metabolism.simulator.model

interface IOrgan {
    fun metabolizeTimeStep(inputs: MetaboliteMap = MetaboliteMap()): MetaboliteMap

    fun getMetabolites(): MetaboliteMap
}