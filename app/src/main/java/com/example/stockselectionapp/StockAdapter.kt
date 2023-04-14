package com.example.stockselectionapp

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StockAdapter(private val stockList: MutableList<Stock>) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.stock_item, parent, false)
        return StockViewHolder(itemView)
    }

    private fun setupChartStyle(chart: LineChart, context: Context) {
        val textColor = if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            Color.WHITE
        } else {
            Color.BLACK
        }
        chart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = textColor // 設置圖例文字顏色
            axisLeft.isEnabled = true
            axisLeft.setDrawGridLines(false)
            axisLeft.textColor = textColor // 設置左軸文字顏色
            axisRight.isEnabled = false
            xAxis.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = textColor // 設置X軸文字顏色
        }
    }

    private fun setupXAxisLabels(chart: LineChart, timeSeries: Map<String, Map<String, String>>) {
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f

            labelRotationAngle = -45f
            setLabelCount(6, true)

            val xAxisLabels = ArrayList<String>()
            for ((key, _) in timeSeries) {
                xAxisLabels.add(key)
            }

            valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        }
    }


    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock: Stock = stockList[position]

        // 提取開盤價和收盤價
        val openPrices: MutableList<Entry> = ArrayList()
        val closePrices: MutableList<Entry> = ArrayList()
        var index = 0f
        for ((_, value) in stock.getTimeSeries().entries) {
            val openPrice = value["1. open"]!!.toFloat()
            val closePrice = value["4. close"]!!.toFloat()
            openPrices.add(Entry(index, openPrice))
            closePrices.add(Entry(index, closePrice))
            index++
        }

        setupChartStyle(holder.stockLineChart, holder.itemView.context)
        holder.stockLineChart.animateX(1500, Easing.EaseInOutQuart)
        setupXAxisLabels(holder.stockLineChart, stock.getTimeSeries())

        // 將數據添加到LineDataSet中
        val openPricesDataSet = LineDataSet(openPrices, "Open Prices")
        openPricesDataSet.color = Color.BLUE
        val closePricesDataSet = LineDataSet(closePrices, "Close Prices")
        closePricesDataSet.color = Color.RED

        // 將LineDataSet添加到LineData中
        val lineData = LineData(openPricesDataSet, closePricesDataSet)

        // 將LineData設置為LineChart的數據源
        holder.stockLineChart.data = lineData
        holder.stockLineChart.invalidate() // 刷新圖表

        holder.tvStockSymbol.text = stock.stockSymbol
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    fun removeItem(position: Int) {
        stockList.removeAt(position)
        notifyItemRemoved(position)
    }

    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStockSymbol: TextView = itemView.findViewById(R.id.tv_stock_symbol)
        val stockLineChart: LineChart = itemView.findViewById(R.id.stock_line_chart)
    }
}
