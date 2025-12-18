package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import jon.sharp.metabolism.simulator.model.body.Body
import jon.sharp.metabolism.simulator.model.body.BodyTransportGraph

/**
 * Main composable for canvas-based graph visualization with integrated organ displays.
 */
@Composable
fun CanvasGraphVisualization(
    transportGraph: BodyTransportGraph,
    body: Body,
    modifier: Modifier = Modifier
) {
    // Merge graph structure with metabolites
    val graphData = remember(transportGraph) {
        createGraphWithMetabolites(transportGraph, body)
    }

    val colorScheme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    // Use BoxWithConstraints to get available width for adaptive layout
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val availableWidthPx = constraints.maxWidth.toFloat()

        // Compute layout based on available width (recalculate when width or data changes)
        val layoutResult = remember(graphData, availableWidthPx) {
            GraphLayoutEngine(graphData, availableWidthPx).computeLayout()
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(layoutResult.canvasHeight.dp)
        ) {
            // Draw all edges first (behind nodes)
            layoutResult.edgePaths.forEach { edgePath ->
                drawGraphEdge(
                    edgePath = edgePath,
                    colorScheme = colorScheme,
                    textMeasurer = textMeasurer
                )
            }

            // Draw all nodes on top
            graphData.forEach { node ->
                val position = layoutResult.nodePositions[node.organName]
                if (position != null) {
                    drawGraphNode(
                        position = position,
                        metabolites = node.metabolites,
                        colorScheme = colorScheme,
                        textMeasurer = textMeasurer
                    )
                }
            }
        }
    }
}
