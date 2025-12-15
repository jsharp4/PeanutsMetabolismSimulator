package jon.sharp.metabolism.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jon.sharp.metabolism.simulator.simulation.SimulationEngine
import jon.sharp.metabolism.simulator.ui.GraphVisualization
import jon.sharp.metabolism.simulator.ui.OrganDisplay
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {

        val engine = remember { SimulationEngine() }
        val body = engine.getBody()

        // State to track if the simulation loop is active
        var isSimulating by remember { mutableStateOf(false) }

        var iterationCount by remember { mutableStateOf(engine.iterationCount()) }

        // Force recomposition by tracking update count
        var updateCount by remember { mutableStateOf(0) }

        // The Logic Engine
        // This block launches whenever 'isSimulating' changes.
        LaunchedEffect(isSimulating) {
            if (isSimulating) {
                // The loop continues as long as the coroutine is active
                // and isSimulating is true
                while (isSimulating) {
                    delay(100L) // Wait first to let simulation update
                    iterationCount = engine.iterationCount()
                    updateCount++ // Force recomposition
                }
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                if (!isSimulating) {
                    engine.runSimulation()
                }
                isSimulating = !isSimulating
            }) {
                // Change button text based on state
                Text(if (isSimulating) "Stop Simulation" else "Start Simulation")
            }

            Text(
                text = "Iteration: $iterationCount (update: $updateCount)",
                style = MaterialTheme.typography.headlineMedium
            )

            // Graph visualization
            GraphVisualization(
                transportGraph = body.getTransportGraph()
            )

            // Display organs programmatically from Body
            body.getTransportGraph().getAllOrgans().forEach { organ ->
                key(organ.getName(), updateCount) {
                    OrganDisplay(
                        body = body,
                        organName = organ.getName()
                    )
                }
            }
        }
    }
}