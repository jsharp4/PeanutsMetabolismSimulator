package jon.sharp.metabolism.simulator.model

abstract class AbstractOrgan(
    val metabolizers: Set<Metabolizer>,
    val outputs: Set<MetaboliteType>
): Organ {
    val metabolitesMap = MetaboliteMap()

    override fun getMetabolites(): MetaboliteMap {
        return metabolitesMap
    }

    override fun metabolizeTimeStep(inputs: MetaboliteMap): MetaboliteMap {
        val outputs = pushOutputs()
        addMetabolitesToPool(inputs)
        metabolizers.forEach { metabolizer ->
            val updatedSubstrates = metabolizer.processSubstrates(metabolitesMap)
            metabolitesMap.updateQuantities(updatedSubstrates)
        }
        return outputs
    }
    private fun addMetabolitesToPool(inputs: MetaboliteMap) {
        metabolitesMap.putOrAdd(inputs)
    }

    private fun pushOutputs(): MetaboliteMap {
        return metabolitesMap.pop(outputs)
    }
}