package jon.sharp.metabolism.simulator.model

import jon.sharp.metabolism.simulator.model.organ.IOrgan

abstract class Organ(
    val metabolizers: Set<IMetabolizer>
): IOrgan {
    val metabolitesMap = MetaboliteMap()

    override open fun getName(): String {
        return this.javaClass.simpleName
    }

    override fun getMetabolites(): MetaboliteMap {
        return metabolitesMap
    }

    override fun metabolizeTimeStep(inputs: MetaboliteMap) {
        addMetabolitesToPool(inputs)
        metabolizers.forEach { metabolizer ->
            val updatedSubstrates = metabolizer.processSubstrates(metabolitesMap)
            metabolitesMap.updateQuantities(updatedSubstrates)
        }
    }
    private fun addMetabolitesToPool(inputs: MetaboliteMap) {
        metabolitesMap.putOrAdd(inputs)
    }
}