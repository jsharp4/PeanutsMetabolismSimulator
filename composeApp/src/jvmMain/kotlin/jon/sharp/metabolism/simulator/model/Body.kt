package jon.sharp.metabolism.simulator.model

import kotlinx.coroutines.delay

actual class Body(
    private val organs: List<Pair<String, Organ>>
) {
    /**
     * Executes a metabolism time step by passing outputs through the organ pipeline.
     * Each organ processes metabolites and passes its outputs to the next organ in sequence.
     */
    actual suspend fun metabolizeTimeStep(initialInputs: MetaboliteMap) {
        var currentInputs = initialInputs
        organs.forEach { (_, organ) ->
            currentInputs = organ.metabolizeTimeStep(currentInputs)
            delay(500L)
        }
    }

    /**
     * Gets the current metabolites for a specific organ by name.
     */
    actual fun getOrganMetabolites(organName: String): MetaboliteMap {
        return organs.find { it.first == organName }?.second?.getMetabolites() ?: MetaboliteMap()
    }

    /**
     * Returns the list of all organ names in pipeline order.
     */
    actual fun getOrganNames(): List<String> {
        return organs.map { it.first }
    }
}
