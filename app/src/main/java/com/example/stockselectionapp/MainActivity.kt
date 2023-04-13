package com.example.stockselectionapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.stockselectionapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var stockAdapter: StockAdapter
    private val stockSymbols = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Set up RecyclerView and adapter
        stockAdapter = StockAdapter(stockSymbols)
        val recyclerView: RecyclerView = binding.root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = stockAdapter

        binding.fab.setOnClickListener {
            showAddStockDialog()
        }

    }
    private fun showAddStockDialog() {
        val input = EditText(this)
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Add Stock Symbol")
            .setMessage("Enter the stock symbol:")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val symbol = input.text.toString().trim()
                if (symbol.isNotEmpty()) {
                    stockSymbols.add(symbol)
                    stockAdapter.notifyItemInserted(stockSymbols.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}