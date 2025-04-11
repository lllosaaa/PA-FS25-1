package ch.zhaw.pa_fs25.userInterface.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CategoryBudgetChart(
    categoryName: String,
    budgetLimit: Double,
    spentAmount: Double,
    modifier: Modifier = Modifier
) {
    val remaining = (budgetLimit + spentAmount).coerceAtLeast(0.0)
    val progress = if (budgetLimit > 0) (-spentAmount / budgetLimit).coerceIn(0.0, 1.0) else 0.0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14f
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2, size.height / 2)

                // Background arc
                drawArc(
                    color = Color.LightGray,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Foreground arc
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = -90f,
                    sweepAngle = (360 * progress).toFloat(),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center value
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${"%.0f".format(remaining)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "CHF",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
