package jon.sharp.metabolism.simulator.model.organ.intestine

import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.IMetabolizer
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import kotlin.math.exp
import kotlin.math.roundToInt

open class PancreaticAmylase: IMetabolizer {

    val reactionRateConstantMinutes = 0.03f

    override fun processSubstrates(inputs: MetaboliteMap, t0: Double): MetaboliteMap {
        val updatedMap = inputs.copy()
        processStarch(updatedMap)
        return updatedMap
    }

    protected open fun processStarch(map: MetaboliteMap) {
        if (map.contains(MetaboliteType.STARCH)) {
            val starchMilliMoles = map.get(MetaboliteType.STARCH)!!.amountMilliMoles
            val starchGrams = PhysicalConstants.millimolesToGrams(starchMilliMoles.toDouble(), PhysicalConstants.MolarMass.STARCH)
            val maltoseGrams = starchGrams * (1 - exp(reactionRateConstantMinutes * -1))
            map.putOrAdd(
                Metabolite(MetaboliteType.MALTOSE,
                    PhysicalConstants.gramsToMillimoles(maltoseGrams, PhysicalConstants.MolarMass.MALTOSE).toFloat(),
                )
            )
            map.updateQuantities(
                Metabolite(
                    MetaboliteType.STARCH,
                    PhysicalConstants.gramsToMillimoles(starchGrams - maltoseGrams, PhysicalConstants.MolarMass.STARCH).toFloat(),
                )
            )
        }



    }
}