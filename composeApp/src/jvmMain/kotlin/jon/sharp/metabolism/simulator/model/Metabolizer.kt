package jon.sharp.metabolism.simulator.model

interface Metabolizer {
    fun processSubstrates(inputs: MetaboliteMap): MetaboliteMap
}