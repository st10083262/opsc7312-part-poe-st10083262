package com.notemaster.models

data class QuotesResponse(
    val record: Record?
)

data class Record(
    val quotes: List<Quote>?
)
