package com.kolecko.koleckonestestiv4

import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint

interface StatisticsController {
    fun getDataGraph(): BarGraphSeries<DataPoint>
    fun getXLabelsGraph(): Array<String>
    fun setDataGraph(data: List<DataPoint>)
    fun setXLabelsGraph(labels: Array<String>)
}

class AllStatisticsControllerImpl(
    private val model: StatisticsModel
) : StatisticsController {

    override fun getDataGraph(): BarGraphSeries<DataPoint> {
        return model.getDataGraph()
    }

    override fun setDataGraph(data: List<DataPoint>) {
        model.setDataGraph(data)
    }

    override fun setXLabelsGraph(labels: Array<String>) {
        model.setXLabelsGraph(labels)
    }

    override fun getXLabelsGraph(): Array<String> {
        return model.getXLabelsGraph()
    }
}