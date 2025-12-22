package jon.sharp.metabolism.simulator.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import jon.sharp.metabolism.simulator.model.body.Body
import jon.sharp.metabolism.simulator.model.body.BodyTransportGraph
import org.jetbrains.compose.resources.imageResource
import peanutsmetabolismsimulator.composeapp.generated.resources.*

/**
 * Maps organ names to their corresponding image resources.
 */
@Composable
fun getOrganImage(organName: String): ImageBitmap? {
    return when (organName) {
        "Blood" -> imageResource(Res.drawable.blood)
        "Cytosol" -> imageResource(Res.drawable.cytosol)
        "Enterocytes" -> imageResource(Res.drawable.enterocytes)
        "Liver" -> imageResource(Res.drawable.liver)
        "MitochondrialInnerMembrane" -> imageResource(Res.drawable.mitochondrial_membrane)
        "MitochondrialMatrix" -> imageResource(Res.drawable.mitochondrion)
        "Mouth" -> imageResource(Res.drawable.mouth)
        "SmallIntestine" -> imageResource(Res.drawable.small_intestine)
        "SmallIntestineLining" -> imageResource(Res.drawable.small_intestine_lining)
        "Stomach" -> imageResource(Res.drawable.stomach)
        "Pancreas" -> imageResource(Res.drawable.pancreas)
        else -> null
    }
}

@Composable
fun CanvasGraphVisualization(
    transportGraph: BodyTransportGraph,
    body: Body,
    modifier: Modifier = Modifier
) {
    // Merge graph structure with metabolites
    val graphData = createGraphWithMetabolites(transportGraph, body)

    // Load organ images
    val organImages = graphData.associate { node ->
        node.organName to getOrganImage(node.organName)
    }

    val colorScheme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val availableWidthPx = constraints.maxWidth.toFloat()

        // Compute layout based on available width
        val layoutResult = remember(graphData, availableWidthPx) {
            GraphLayoutEngine(graphData, availableWidthPx).computeLayout()
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(layoutResult.canvasHeight.dp)
        ) {
            // PASS 1: Draw all edge lines (Bottom Layer)
            layoutResult.edgePaths.forEach { edgePath ->
                drawGraphEdge(
                    edgePath = edgePath,
                    colorScheme = colorScheme
                )
            }

            // PASS 2: Draw all nodes (Middle Layer)
            graphData.forEach { node ->
                val position = layoutResult.nodePositions[node.organName]
                if (position != null) {
                    drawGraphNode(
                        position = position,
                        metabolites = node.metabolites,
                        colorScheme = colorScheme,
                        textMeasurer = textMeasurer,
                        organImage = organImages[node.organName]
                    )
                }
            }

            // PASS 3: Draw all edge labels (Top Layer - "Sent to Front")
            layoutResult.edgePaths.forEach { edgePath ->
                if (edgePath.metabolites.isNotEmpty()) {
                    // Calculate a position that doesn't overlap any nodes
                    val labelPosition = calculateLabelPosition(
                        waypoints = edgePath.waypoints,
                        nodePositions = layoutResult.nodePositions
                    )

                    drawEdgeLabel(
                        position = labelPosition,
                        metabolites = edgePath.metabolites,
                        color = edgePath.color,
                        textMeasurer = textMeasurer,
                        colorScheme = colorScheme
                    )
                }
            }
        }
    }
}
