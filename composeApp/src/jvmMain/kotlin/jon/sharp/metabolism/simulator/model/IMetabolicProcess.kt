package jon.sharp.metabolism.simulator.model

interface IMetabolizer {
    fun processSubstrates(inputs: MetaboliteMap, t0: Double): MetaboliteMap
}