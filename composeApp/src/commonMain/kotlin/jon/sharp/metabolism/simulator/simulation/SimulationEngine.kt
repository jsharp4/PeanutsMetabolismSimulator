package jon.sharp.metabolism.simulator.simulation

import jon.sharp.metabolism.simulator.model.Body

expect class SimulationEngine() {
    fun runSimulation()

    fun getBody(): Body

    fun iterationCount(): Int
}
