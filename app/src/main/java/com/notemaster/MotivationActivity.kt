package com.notemaster

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.notemaster.models.Quote
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MotivationActivity : AppCompatActivity() {

    private lateinit var quotesRecyclerView: RecyclerView
    private lateinit var quoteAdapter: QuoteAdapter
    private var quotes: List<Quote> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation)

        quotesRecyclerView = findViewById(R.id.quotesRecyclerView)
        quotesRecyclerView.layoutManager = LinearLayoutManager(this)
        quoteAdapter = QuoteAdapter(quotes)
        quotesRecyclerView.adapter = quoteAdapter

        fetchQuotes()
    }

    private fun fetchQuotes() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.jsonbin.io/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(QuotesApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.getQuotes()
                Log.d("MotivationActivity", "Raw JSON: ${Gson().toJson(response)}")

                // Check if the response contains quotes
                val fetchedQuotes = response.record?.quotes
                if (!fetchedQuotes.isNullOrEmpty()) {
                    quotes = fetchedQuotes
                    quoteAdapter.updateQuotes(quotes)
                    Log.d("MotivationActivity", "Quotes loaded successfully.")
                } else {
                    Toast.makeText(this@MotivationActivity, "No quotes available", Toast.LENGTH_SHORT).show()
                    Log.d("MotivationActivity", "No quotes available in response.")
                }
            } catch (e: Exception) {
                Toast.makeText(this@MotivationActivity, "Failed to load quotes: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MotivationActivity", "Error loading quotes", e)
            }
        }
    }
}
