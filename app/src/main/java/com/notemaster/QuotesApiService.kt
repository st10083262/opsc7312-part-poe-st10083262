package com.notemaster

import com.notemaster.models.QuotesResponse
import retrofit2.http.GET

interface QuotesApiService {
    @GET("b/672b3bf6acd3cb34a8a39ae1")
    suspend fun getQuotes(): QuotesResponse
}

