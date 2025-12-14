package jon.sharp.metabolism.simulator.simulation

import jon.sharp.metabolism.simulator.model.Body
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.organ.cells.Blood
import jon.sharp.metabolism.simulator.model.organ.cells.Cytosol
import jon.sharp.metabolism.simulator.model.organ.cells.mitochondria.MitochondrialInnerMembrane
import jon.sharp.metabolism.simulator.model.organ.cells.mitochondria.MitochondrialMatrix
import jon.sharp.metabolism.simulator.model.organ.cells.stomach.Stomach
import jon.sharp.metabolism.simulator.model.organ.intestine.SmallIntestine
import jon.sharp.metabolism.simulator.model.organ.intestine.SmallIntestineLining
import jon.sharp.metabolism.simulator.model.organ.mouth.Mouth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

actual class SimulationEngine {
    private val bloodOrgan = Blood()

    private val body = Body(
        listOf(
            "Mouth" to Mouth(),
            "Stomach" to Stomach(),
            "Small Intestine" to SmallIntestine(),
            "Small Intestine Lining" to SmallIntestineLining(),
            "Cytosol" to Cytosol(),
            "Mitochondrial Matrix" to MitochondrialMatrix(),
            "Inner Matrix Membrane" to MitochondrialInnerMembrane(),
            "Blood" to bloodOrgan
        )
    )

    private var iterations = 0

    actual fun runSimulation() {
        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            // Initial time step with starch input
            body.metabolizeTimeStep(
                MetaboliteMap(
                    Metabolite(
                        MetaboliteType.STARCH,
                        0.5f
                    ),
                    Metabolite(
                        MetaboliteType.ARACHIN,
                        amountMilliMoles = PhysicalConstants.gramsToMillimoles(
                            50.0,
                            PhysicalConstants.ARACHIN.MOLAR_MASS
                        ).toFloat()
                    )
                )
            )
            iterations++
            delay(2000L)
            // Continue simulation
            while (true) {
                //delay(100L)
                body.metabolizeTimeStep(MetaboliteMap(
                    Metabolite(
                        MetaboliteType.STARCH,
                        0.0f//Random.nextFloat() / 2
                    )
                ))
                iterations++
            }
        }
    }

    actual fun getBody(): Body {
        return body
    }

    actual fun iterationCount(): Int {
        return iterations
    }
}