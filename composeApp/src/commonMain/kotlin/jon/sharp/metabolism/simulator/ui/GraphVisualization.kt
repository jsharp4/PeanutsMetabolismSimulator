package jon.sharp.metabolism.simulator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jon.sharp.metabolism.simulator.model.body.BodyTransportGraph
import jon.sharp.metabolism.simulator.model.body.GraphEdge
import jon.sharp.metabolism.simulator.model.body.GraphNode
import jon.sharp.metabolism.simulator.model.body.MetaboliteTransfer

@Composable
fun GraphVisualization(
    transportGraph: BodyTransportGraph,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Transport Graph Structure",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Get all nodes from the graph
            val allNodes = transportGraph.getGraphStructure()

            allNodes.forEach { node ->
                NodeVisualization(node)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun NodeVisualization(
    node: GraphNode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        // Node name
        Text(
            text = node.organName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Edges
        if (node.edges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            node.edges.forEach { edge ->
                EdgeVisualization(edge)
                Spacer(modifier = Modifier.height(4.dp))
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "└─ No outgoing edges (sink node)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun EdgeVisualization(
    edge: GraphEdge,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        // Arrow and destination
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = edge.destinationName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Metabolites on this edge
        edge.metabolites.forEach { metabolite ->
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, top = 2.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "• ${metabolite.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${metabolite.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
