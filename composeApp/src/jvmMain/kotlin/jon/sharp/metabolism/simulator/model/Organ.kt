package jon.sharp.metabolism.simulator.model

import jon.sharp.metabolism.simulator.model.organ.IOrgan

data class PercentToOutput(
    val percent: Float,
)

abstract class Organ(
    val metabolizers: Set<IMetabolizer>,
    val outputs: Map<MetaboliteType, PercentToOutput>
): IOrgan {
    val metabolitesMap = MetaboliteMap()

    override fun getName(): String {
        return this.javaClass.name
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

    private fun pushOutputs(): MetaboliteMap {
        val itemsToOutput = metabolitesMap.pop(outputs.keys)
        val quantitiesToOutput = itemsToOutput.getAll().map { it ->
            val outputAmount = it.amountMilliMoles * outputs[it.type]!!.percent / 100
            it.amountMilliMoles = it.amountMilliMoles - outputAmount

            Metabolite(
                it.type,
                outputAmount,
            )
        }
        metabolitesMap.updateQuantities(itemsToOutput)
        return MetaboliteMap(quantitiesToOutput.toSet())
    }
}