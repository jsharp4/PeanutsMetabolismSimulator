package jon.sharp.metabolism.simulator.model

interface Organ {
    fun metabolizeTimeStep(inputs: MetaboliteMap = MetaboliteMap()): MetaboliteMap

    fun getMetabolites(): MetaboliteMap
}