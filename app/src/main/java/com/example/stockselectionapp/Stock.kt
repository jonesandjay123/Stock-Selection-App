package com.example.stockselectionapp

class Stock(
    val stockSymbol: String,
    private val timeSeriesData: Map<String, Map<String, String>>
) {
    fun getTimeSeries(): Map<String, Map<String, String>> {
        return timeSeriesData
    }
}