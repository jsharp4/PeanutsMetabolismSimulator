package jon.sharp.metabolism.simulator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jon.sharp.metabolism.simulator.model.Body
import jon.sharp.metabolism.simulator.model.Metabolite

@Composable
fun OrganDisplay(
    body: Body,
    organName: String,
    imagePainter: Painter? = null,
    modifier: Modifier = Modifier
) {
    val metabolites = body.getOrganMetabolites(organName).getAll().toList()
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imagePainter != null) {
                Image(
                    painter = imagePainter,
                    contentDescription = "$organName organ",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = organName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                MetabolitesList(metabolites)
            }
        }
    }
}

fun formatWeight(weight: Float): String {
    return when {
        weight == 0f -> "0.00 mmol"
        weight >= 0.01f -> "%.2f mmol".format(weight)
        weight >= 0.0001f -> "%.4f mmol".format(weight)
        else -> "%.2e mmol".format(weight) // Scientific notation for very small values
    }
}

@Composable
fun MetabolitesList(metabolites: List<Metabolite>) {
    if (metabolites.isEmpty()) {
        Text(
            text = "No metabolites",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    } else {
        Column {
            metabolites.forEach { metabolite ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = metabolite.type.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatWeight(metabolite.amountMilliMoles),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}