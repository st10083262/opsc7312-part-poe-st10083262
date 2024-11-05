package com.notemaster

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = "",  // Firestore ID or a locally generated unique ID
    val title: String = "",
    val content: String = "",
    val date: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false  // Sync status for offline mode
) : Parcelable
