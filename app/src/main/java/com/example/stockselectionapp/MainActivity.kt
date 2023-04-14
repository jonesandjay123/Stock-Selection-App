package com.example.stockselectionapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.stockselectionapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var stockAdapter: StockAdapter
    private val stockSymbols = mutableListOf<String>()

    private val client = OkHttpClient()

    private fun readApiSecrets(): String {
        return try {
            assets.open("secrets.properties").use { inputStream ->
                val properties = Properties().apply {
                    load(inputStream)
                }
                properties.getProperty("access_token")
                    ?: throw IllegalStateException("Failed to find 'access_token' in secrets.properties")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read secrets.properties", e)
        }
    }

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

        // Set the version number text
        initializeVersionTextView()

        binding.fab.setOnClickListener {
            showAddStockDialog()
        }

        // Initialize ItemTouchHelper and attach it to RecyclerView
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // Do nothing, since we are only interested in swipe to delete
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                stockAdapter.removeItem(position)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun initializeVersionTextView() {
        val versionTextView = findViewById<TextView>(R.id.version_text)
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName

        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMddHHmmss", Locale.getDefault())
        val timeStampString = sdf.format(Date(timestamp)).takeLast(4)

        versionTextView.text = "Version $versionName.$timeStampString"
    }

    private fun fetchStockData(symbol: String) {
        val apiKey = readApiSecrets()
        val url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=$symbol&interval=5min&apikey=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val jsonData = response.body?.string()
                    println(jsonData)
                }
            }
        })
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
                    fetchStockData(symbol)
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

}