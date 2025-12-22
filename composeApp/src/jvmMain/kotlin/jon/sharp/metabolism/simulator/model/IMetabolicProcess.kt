package jon.sharp.metabolism.simulator.model

interface IMetabolicProcess {
    fun processSubstrates(inputs: MetaboliteMap, t0: Double): MetaboliteMap
}