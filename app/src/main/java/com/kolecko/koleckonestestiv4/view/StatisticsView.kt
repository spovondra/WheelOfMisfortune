package com.kolecko.koleckonestestiv4

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter

interface StatisticsView {
    fun showGraph()
}

class StatisticsViewImpl : StatisticsView, AppCompatActivity() {
    private lateinit var controller: StatisticsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // ActionBar + return button
        //supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Statistiky"
        supportActionBar?.elevation = 0f

        controller = AllStatisticsControllerImpl(StatisticsModelImp())

        showGraph()
    }

    override fun showGraph() {
        // Find the "Clear Data" button
        val clearDataButton = findViewById<Button>(R.id.clearDataButton)

        // Get a reference to your GraphView from the XML layout
        val graphView = findViewById<GraphView>(R.id.graph)

        graphView.addSeries(controller.getDataGraph())

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

        staticLabelsFormatter.setHorizontalLabels(controller.getXLabelsGraph())
        graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter

        clearDataButton.setOnClickListener {
            // Clear the data in the graph
            controller.getDataGraph().resetData(arrayOf())

            // Reset X labels
            controller.setXLabelsGraph(arrayOf("Label 1", "Label 2"))
            staticLabelsFormatter.setHorizontalLabels(arrayOf("Label 1", "Label 2"))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
