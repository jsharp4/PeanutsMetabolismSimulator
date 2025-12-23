package jon.sharp.metabolism.simulator.model

import jon.sharp.metabolism.simulator.model.organ.IOrgan

abstract class Organ(
    val metabolicProcesses: List<IMetabolicProcess>,
    val initialMetabolites: MetaboliteMap = MetaboliteMap(),
    val readOnlyMetabolites: Set<MetaboliteType> = setOf()
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
        if (this.javaClass.simpleName == "Pancreas") {
            println("hi")
        }
        addMetabolitesToPool(inputs)
        metabolicProcesses.forEach { metabolizer ->
            val updatedSubstrates = metabolizer.processSubstrates(metabolitesMap, t0)
            metabolitesMap.updateQuantities(updatedSubstrates)
        }
        readOnlyMetabolites.forEach { type ->
            if (metabolitesMap.contains(type)) metabolitesMap.updateQuantities(
                Metabolite(
                    type,
                    0.0
                )
            )
        }
    }
    private fun addMetabolitesToPool(inputs: MetaboliteMap) {
        inputs.getAll().forEach {
            it ->
            if (it.updateType == UpdateType.OVERWRITE) {
                metabolitesMap.updateQuantities(it)
            }
            else metabolitesMap.putOrAdd(it)
        }
    }
}