package jon.sharp.metabolism.simulator.model

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

class MetaboliteMap(
    set: Set<Metabolite>  = setOf()
) {
    constructor(vararg metabolites: Metabolite) : this(metabolites.toSet())

    private val map = ConcurrentHashMap<MetaboliteType, Metabolite>()

    init {
        setToNewMap(set)
    }

    fun contains(type: MetaboliteType): Boolean {
        return map.containsKey(type)
    }

    fun putOrAdd(metabolites: MetaboliteMap) {
        metabolites.getAll().forEach { metabolite ->
            if (map.containsKey(metabolite.type)) {
                map[metabolite.type]!!.amountMilliMoles += metabolite.amountMilliMoles
            } else {
                map[metabolite.type] = metabolite
            }
        }
    }

    fun putOrAdd(vararg metabolites: Metabolite) {
        putOrAdd(MetaboliteMap(*metabolites))
    }

    fun removeIfPresent(metabolites: MetaboliteMap) {
        metabolites.getAll().forEach { metabolite ->
            if (map.containsKey(metabolite.type)) {
                map[metabolite.type]!!.amountMilliMoles = max(
                    map[metabolite.type]!!.amountMilliMoles - metabolite.amountMilliMoles, 0.0f
                )
            }
        }
    }

    operator fun get(metaboliteType: MetaboliteType): Metabolite? {
        return map[metaboliteType]
    }

    fun getAll(): Set<Metabolite> {
        return map.values.toSet()
    }

    fun updateQuantities(metabolites: MetaboliteMap) {
        metabolites.getAll().forEach { metabolite ->
            map[metabolite.type] = metabolite
        }
    }

    fun updateQuantities(vararg metabolites: Metabolite) {
        updateQuantities(MetaboliteMap(*metabolites))
    }

    fun pop(metabolites: Set<MetaboliteType>): MetaboliteMap {
        return MetaboliteMap(metabolites.mapNotNull { type ->
            map.remove(type)
        }.toSet())
    }

    fun copy(): MetaboliteMap {
        val copiedSet = this.getAll().map { it.copy() }.toSet()
        return MetaboliteMap(copiedSet)
    }

    private fun setToNewMap(set: Set<Metabolite>) {
        set.forEach { metabolite ->
            map[metabolite.type] = metabolite
        } 
    }

    override fun toString(): String {
        return map.values.toSet().toString()
    }
}