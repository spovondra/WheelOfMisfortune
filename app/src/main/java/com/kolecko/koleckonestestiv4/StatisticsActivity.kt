package com.kolecko.koleckonestestiv4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.graphics.Paint
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.BarGraphSeries
class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // ActionBar + return button
        //supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Statistiky"
        supportActionBar?.elevation = 0f

        // Find the "Clear Data" button
        val clearDataButton = findViewById<Button>(R.id.clearDataButton)

        // Get a reference to your GraphView from the XML layout
        val graphView = findViewById<GraphView>(R.id.graph)

        // Create a series for your graph
        val series = BarGraphSeries(arrayOf(
            DataPoint(1.0, 10.0),
            DataPoint(2.0, 20.0),
            DataPoint(3.0, 30.0),
            DataPoint(4.0, 40.0),
            DataPoint(5.0, 50.0),
            DataPoint(6.0, 20.0),
        ))

        graphView.addSeries(series)

        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(0.5)
        graphView.viewport.setMaxX(6.5)

        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(50.5)

        graphView.gridLabelRenderer.numHorizontalLabels = 5
        graphView.gridLabelRenderer.numVerticalLabels = 5

        //graphView.gridLabelRenderer.horizontalAxisTitle = "Body"
        // Nastavení výšky pro popisky na ose X
        graphView.gridLabelRenderer.labelHorizontalHeight = 25

        graphView.gridLabelRenderer.setHorizontalLabelsAngle(45)
        val staticLabelsFormatter = StaticLabelsFormatter(graphView)
        val xLabels = arrayOf(
            "6.10","7.10", "8.10", "9.10", "10.10","11.10", "12.10"
        )
        staticLabelsFormatter.setHorizontalLabels(xLabels)
        graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter

        clearDataButton.setOnClickListener {
            // Clear the data in the graph
            series.resetData(arrayOf())
            val xLabels = arrayOf("0.0", "0.0")
            staticLabelsFormatter.setHorizontalLabels(xLabels)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
