package jon.sharp.metabolism.simulator.model

data class PercentToOutput(
    val percent: Float,
)

abstract class AbstractOrgan(
    val metabolizers: Set<Metabolizer>,
    val outputs: Map<MetaboliteType, PercentToOutput>
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