package jon.sharp.metabolism.simulator.model.organ.mouth

import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.Metabolizer
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.organ.intestine.PancreaticAmylase
import kotlin.math.exp

class SalivaryAmylase: PancreaticAmylase() {

    val timeInMouthMinutes = 0.17


    override fun processStarch(map: MetaboliteMap) {
        if (map.contains(MetaboliteType.STARCH)) {
            val starchMilliMoles = map[MetaboliteType.STARCH]!!.amountMilliMoles
            val starchGrams =
                PhysicalConstants.millimolesToGrams(starchMilliMoles.toDouble(), PhysicalConstants.Starch.MOLAR_MASS)
            val maltoseGrams = starchGrams * (1 - exp(reactionRateConstantMinutes * -1 * timeInMouthMinutes))
            map.putOrAdd(
                Metabolite(
                    MetaboliteType.MALTOSE,
                    PhysicalConstants.gramsToMillimoles(maltoseGrams, PhysicalConstants.Maltose.MOLAR_MASS).toFloat(),
                )
            )
            map.updateQuantities(
                Metabolite(
                    MetaboliteType.STARCH,
                    PhysicalConstants.gramsToMillimoles(starchGrams - maltoseGrams, PhysicalConstants.Starch.MOLAR_MASS)
                        .toFloat(),
                )
            )
        }
    }
}