package jon.sharp.metabolism.simulator.model

import jon.sharp.metabolism.simulator.model.organ.IOrgan

abstract class Organ(
    val metabolizers: List<IMetabolizer>,
    val initialMetabolites: MetaboliteMap = MetaboliteMap()
): IOrgan {
    val metabolitesMap = MetaboliteMap()

    init {
        metabolitesMap.putOrAdd(initialMetabolites)
    }

    open override fun getName(): String {
        return this.javaClass.simpleName
    }

    override fun getMetabolites(): MetaboliteMap {
        return metabolitesMap
    }

    override fun metabolizeTimeStep(inputs: MetaboliteMap, t0: Double) {
        addMetabolitesToPool(inputs)
        metabolizers.forEach { metabolizer ->
            val updatedSubstrates = metabolizer.processSubstrates(metabolitesMap, t0)
            metabolitesMap.updateQuantities(updatedSubstrates)
        }
    }
    private fun addMetabolitesToPool(inputs: MetaboliteMap) {
        metabolitesMap.putOrAdd(inputs)
    }
}