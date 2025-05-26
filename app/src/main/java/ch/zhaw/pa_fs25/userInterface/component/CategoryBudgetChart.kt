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
    val remaining = budgetLimit + spentAmount
    val progress = if (budgetLimit > 0) (-spentAmount / budgetLimit).coerceIn(0.0, 1.0) else 0.0

    val progressPercent = (progress * 100).toInt()
    val centerTextColor = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val ringColor = if (remaining < 0) Color.Red else Color(0xFF4CAF50)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {

        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14f
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2, size.height / 2)

                drawArc(
                    color = Color.LightGray,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = (360 * progress).toFloat(),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    color = centerTextColor
                )
                Text(
                    text = "used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (remaining >= 0)
                "Remaining: ${"%.2f".format(remaining)} CHF"
            else
                "Over: ${"%.2f".format(-remaining)} CHF",
            style = MaterialTheme.typography.bodySmall,
            color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
