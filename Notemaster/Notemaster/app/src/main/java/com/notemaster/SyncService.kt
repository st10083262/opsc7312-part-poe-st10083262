// SyncService.kt
package com.notemaster

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.notemaster.data.NoteDatabase
import com.notemaster.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncService(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val noteDao = NoteDatabase.getInstance(context).noteDao()

    fun syncNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val unsyncedNotes = noteDao.getUnsyncedNotes()
            for (note in unsyncedNotes) {
                val noteMap = mapOf(
                    "title" to note.title,
                    "content" to note.content,
                    "date" to note.date
                )

                db.collection("notes")
                    .add(noteMap)
                    .addOnSuccessListener { documentReference ->
                        CoroutineScope(Dispatchers.IO).launch {
                            noteDao.update(note.copy(id = documentReference.id, isSynced = true))
                        }
                    }
                    .addOnFailureListener {
                        // Log or handle sync failure
                    }
            }
        }
    }
}
