// CreateNoteActivity.kt
package com.notemaster

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.notemaster.data.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val noteDao by lazy { NoteDatabase.getInstance(this).noteDao() }
    private val channelId = "note_notifications_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        db = FirebaseFirestore.getInstance()

        val titleEditText: EditText = findViewById(R.id.titleEditText)
        val contentEditText: EditText = findViewById(R.id.contentEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val noteId = UUID.randomUUID().toString()  // Unique ID for local storage
            val note = Note(
                id = noteId,
                title = title,
                content = content,
                date = System.currentTimeMillis(),
                isSynced = isOnline()
            )

            if (isOnline()) {
                saveNoteToFirestore(note)
            } else {
                saveNoteToLocalDatabase(note)
            }
        }
    }

    private fun isOnline(): Boolean {
        // Implement actual network check here
        return true
    }

    private fun saveNoteToFirestore(note: Note) {
        val noteMap = mapOf(
            "title" to note.title,
            "content" to note.content,
            "date" to note.date
        )

        db.collection("notes")
            .add(noteMap)
            .addOnSuccessListener { documentReference ->
                lifecycleScope.launch(Dispatchers.IO) {
                    noteDao.update(note.copy(id = documentReference.id, isSynced = true))
                }
                Toast.makeText(this, "Note saved to Firestore", Toast.LENGTH_SHORT).show()
                sendNoteAddedNotification()
                finishActivityWithResult(note.copy(id = documentReference.id))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save note to Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveNoteToLocalDatabase(note: Note) {
        lifecycleScope.launch(Dispatchers.IO) {
            noteDao.insert(note)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CreateNoteActivity, "Note saved locally", Toast.LENGTH_SHORT).show()
                sendNoteAddedNotification()
                finishActivityWithResult(note)
            }
        }
    }

    private fun finishActivityWithResult(note: Note) {
        val resultIntent = Intent().apply {
            putExtra("NEW_NOTE", note)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun sendNoteAddedNotification() {
        val title = getString(R.string.new_note_notification_title)
        val message = getString(R.string.new_note_notification_body)

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                return // Exit the method if permission is not granted
            }
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notemaster)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    // Handle the permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, send the notification
            sendNoteAddedNotification()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
