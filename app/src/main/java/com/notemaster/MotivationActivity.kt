package com.notemaster

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.notemaster.models.Quote
import com.notemaster.models.QuotesResponse
import okhttp3.*
import java.io.IOException

class MotivationActivity : AppCompatActivity() {

    private lateinit var quotesRecyclerView: RecyclerView
    private lateinit var quoteAdapter: QuoteAdapter
    private var quotes: List<Quote> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation)

        quotesRecyclerView = findViewById(R.id.quotesRecyclerView)
        quotesRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchQuotes()
    }

    private fun fetchQuotes() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.jsonbin.io/v3/b/672a82bfad19ca34f8c4ce52")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MotivationActivity, "Failed to load quotes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { jsonString ->
                        try {
                            val quotesResponse = Gson().fromJson(jsonString, QuotesResponse::class.java)
                            quotes = quotesResponse.quotes ?: emptyList()
                            runOnUiThread {
                                if (quotes.isNotEmpty()) {
                                    quoteAdapter = QuoteAdapter(quotes)
                                    quotesRecyclerView.adapter = quoteAdapter
                                } else {
                                    Toast.makeText(this@MotivationActivity, "No quotes available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JsonSyntaxException) {
                            runOnUiThread {
                                Toast.makeText(this@MotivationActivity, "Failed to parse quotes", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MotivationActivity, "Failed to load quotes: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
