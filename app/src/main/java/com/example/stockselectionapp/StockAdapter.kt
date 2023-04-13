package com.example.stockselectionapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stockselectionapp.R

class StockAdapter(private val stockSymbols: MutableList<String>) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.stock_item, parent, false)
        return StockViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.tvStockSymbol.text = stockSymbols[position]
    }

    override fun getItemCount(): Int {
        return stockSymbols.size
    }

    fun removeItem(position: Int) {
        stockSymbols.removeAt(position)
        notifyItemRemoved(position)
    }

    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStockSymbol: TextView = itemView.findViewById(R.id.tv_stock_symbol)
    }
}