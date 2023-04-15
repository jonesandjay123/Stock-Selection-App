package com.example.stockselectionapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.stockselectionapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityMainBinding
    private lateinit var stockAdapter: StockAdapter
    private val stockSymbols = mutableListOf<Stock>()

    private val client = OkHttpClient()
    private val job = Job()

    // 添加此行以將MainActivity設置為CoroutineScope
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

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

    private suspend fun fetchStockData(symbol: String): Map<String, Map<String, String>>? {
        val apiKey = readApiSecrets()
        val url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=$symbol&interval=5min&apikey=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()
                val jsonObject = JSONObject(jsonData)
                val timeSeries = jsonObject.getJSONObject("Time Series (5min)")

                val result = mutableMapOf<String, Map<String, String>>()
                for (key in timeSeries.keys()) {
                    val value = timeSeries.getJSONObject(key)
                    val entries = value.keys().asSequence().associateWith { value.getString(it) }
                    result[key] = entries
                }
                result
            } catch (e: Exception) {
                null
            }
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
                    launch {
                        val timeSeries = fetchStockData(symbol)
                        if (timeSeries != null) {
                            stockSymbols.add(Stock(symbol, timeSeries))
                            stockAdapter.notifyItemInserted(stockSymbols.size - 1)
                        } else {
                            showToast("Invalid stock symbol or data not available.")
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}