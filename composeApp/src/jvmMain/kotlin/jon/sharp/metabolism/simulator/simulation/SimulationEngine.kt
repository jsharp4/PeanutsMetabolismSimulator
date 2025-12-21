package jon.sharp.metabolism.simulator.simulation

import jon.sharp.metabolism.simulator.model.body.Body
import jon.sharp.metabolism.simulator.model.Metabolite
import jon.sharp.metabolism.simulator.model.MetaboliteMap
import jon.sharp.metabolism.simulator.model.MetaboliteType
import jon.sharp.metabolism.simulator.model.PhysicalConstants
import jon.sharp.metabolism.simulator.model.body.BodyTransportGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class SimulationEngine {

    private val body = Body(
        BodyTransportGraph()
    )


    private var iterations = 0.0

    actual fun runSimulation() {
        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            // Initial time step with starch input
            body.metabolizeTimeStep(
                MetaboliteMap(
                    Metabolite(
                        MetaboliteType.STARCH,
                        PhysicalConstants.gramsToMillimoles(
                            2.0,
                            PhysicalConstants.MolarMass.STARCH
                        )
                    ),
                    Metabolite(
                        MetaboliteType.ARACHIN,
                        amountMilliMoles = PhysicalConstants.gramsToMillimoles(
                            13.0,
                            PhysicalConstants.MolarMass.ARACHIN
                        )
                    ),
                    Metabolite(
                        MetaboliteType.TRIOLEIN,
                        PhysicalConstants.gramsToMillimoles(
                            12.0,
                            PhysicalConstants.MolarMass.TRIOLEIN
                        )
                    )
                ),
                iterations++
            )
            delay(100L)
            // Continue simulation
            while (true) {
                delay(100L)
                body.metabolizeTimeStep(MetaboliteMap(
                    Metabolite(
                        MetaboliteType.STARCH,
                        0.0
                        )
                     ),
                    iterations++
                )
            }
        }
    }

    actual fun getBody(): Body {
        return body
    }

    actual fun iterationCount(): Int {
        return iterations.toInt()
    }
}