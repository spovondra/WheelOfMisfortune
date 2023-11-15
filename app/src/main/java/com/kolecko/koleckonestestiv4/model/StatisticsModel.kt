package com.kolecko.koleckonestestiv4

import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint

interface StatisticsModel {
    fun getDataGraph(): BarGraphSeries<DataPoint>
    fun getXLabelsGraph(): Array<String>
    fun setXLabelsGraph(labels: Array<String>)
    fun setDataGraph(data: List<DataPoint>)
}

class StatisticsModelImp : StatisticsModel {

    private val dataGraph = BarGraphSeries(arrayOf(DataPoint(1.0, 10.0),
        DataPoint(2.0, 20.0),
        DataPoint(3.0, 30.0),
        DataPoint(4.0, 40.0),
        DataPoint(5.0, 50.0),
        DataPoint(6.0, 20.0),
    ))

    private val xLabels = arrayOf(
        "6.10", "7.10", "8.10", "9.10", "10.10", "11.10", "12.10"
    )

    override fun getDataGraph(): BarGraphSeries<DataPoint> {
        return dataGraph
    }

    override fun getXLabelsGraph(): Array<String> {
        return xLabels
    }

    override fun setXLabelsGraph(labels: Array<String>) {
        this.xLabels.copyOf(labels.size)
        labels.copyInto(this.xLabels)
    }

    override fun setDataGraph(data: List<DataPoint>) {
        this.dataGraph.resetData(data.toTypedArray())
    }
}
