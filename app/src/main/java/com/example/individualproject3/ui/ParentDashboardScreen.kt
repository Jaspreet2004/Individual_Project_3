package com.example.individualproject3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.individualproject3.data.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * Dashboard for Parents to view child progress.
 * Includes a BarChart of attempts/scores and a log viewer.
 *
 * @param navController Navigation controller.
 * @param onLogout Callback for logout.
 */
@Composable
fun ParentDashboardScreen(navController: NavController, onLogout: () -> Unit) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    
    val viewModel: ParentDashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                 @Suppress("UNCHECKED_CAST")
                return ParentDashboardViewModel(database.gameDao(), database.userDao(), context) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "Parent Dashboard", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Child Progress", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Chart Card
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Attempts per Level", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val chartColor = MaterialTheme.colorScheme.primary.toArgb()
                val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

                // Prepare Data
                val levelStats = uiState.levelStats.sortedBy { it.levelName }
                val entries = levelStats.mapIndexed { index, stat ->
                    BarEntry(index.toFloat(), stat.attemptCount.toFloat())
                }
                
                // Map full names and short IDs for cleaner labels
                val labels = levelStats.map { stat ->
                    com.example.individualproject3.logic.Levels.AllLevels.find { it.name == stat.levelName }?.id ?: stat.levelName
                }

                if (entries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                         Text("No data available yet.", color = Color.Gray)
                    }
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                description.isEnabled = false
                                setTouchEnabled(false)
                                setDrawGridBackground(false)
                                setDrawBorders(false)
                                
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.setDrawGridLines(false)
                                xAxis.granularity = 1f
                                xAxis.textColor = textColor
                                xAxis.labelCount = labels.size // Ensure all labels show
                                
                                // Use value formatter to show level IDs
                                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                                
                                axisLeft.setDrawGridLines(true)
                                axisLeft.textColor = textColor
                                axisLeft.axisMinimum = 0f 
                                axisLeft.granularity = 1f
                                axisRight.isEnabled = false
                                
                                legend.isEnabled = false 
                                
                                val dataSet = BarDataSet(entries, "Attempts")
                                dataSet.color = chartColor
                                dataSet.valueTextSize = 12f
                                dataSet.valueTextColor = textColor
                                // Integer formatter for values
                                dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        return value.toInt().toString()
                                    }
                                }
                                
                                data = BarData(dataSet)
                                invalidate()
                            }
                        },
                        update = { chart ->
                            val newStats = uiState.levelStats.sortedBy { it.levelName }
                            val newEntries = newStats.mapIndexed { index, stat ->
                                BarEntry(index.toFloat(), stat.attemptCount.toFloat())
                            }
                            // Recalculate labels for updates
                            val newLabels = newStats.map { stat ->
                                com.example.individualproject3.logic.Levels.AllLevels.find { it.name == stat.levelName }?.id ?: stat.levelName
                            }
                            
                            if (newEntries.isNotEmpty()) {
                                chart.xAxis.valueFormatter = IndexAxisValueFormatter(newLabels)
                                chart.xAxis.labelCount = newLabels.size
                                val set = BarDataSet(newEntries, "Attempts")
                                set.color = chartColor
                                set.valueTextSize = 12f
                                set.valueTextColor = textColor
                                set.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        return value.toInt().toString()
                                    }
                                }
                                chart.data = BarData(set)
                                chart.notifyDataSetChanged()
                                chart.invalidate()
                            }
                        }
                    )
                }
            }
        }
    }
}
