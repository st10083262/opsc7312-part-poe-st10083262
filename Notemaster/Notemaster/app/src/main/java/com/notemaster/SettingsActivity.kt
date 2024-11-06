package com.notemaster

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import java.util.concurrent.Executor

class SettingsActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var uploadImageButton: MaterialButton
    private lateinit var darkModeSwitch: SwitchMaterial
    private lateinit var biometricSwitch: SwitchMaterial
    private lateinit var languageRadioGroup: RadioGroup

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences for settings
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        // Initialize UI elements
        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        profileImageView = findViewById(R.id.profileImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        biometricSwitch = findViewById(R.id.biometricSwitch)
        languageRadioGroup = findViewById(R.id.languageRadioGroup)

        // Load saved settings
        loadSettings()

        // Fetch user details from Firestore
        fetchUserDetails()

        // Handle image upload button click
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        // Toggle for dark mode
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // Set up biometric authentication toggle
        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                authenticateWithBiometrics()
            } else {
                sharedPreferences.edit().putBoolean("biometric_enabled", false).apply()
                Toast.makeText(this, "Biometric login disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up language selection radio group
        val savedLanguage = sharedPreferences.getString("language", "en")
        when (savedLanguage) {
            "en" -> findViewById<RadioButton>(R.id.radioEnglish).isChecked = true
            "zu" -> findViewById<RadioButton>(R.id.radioZulu).isChecked = true
            "xh" -> findViewById<RadioButton>(R.id.radioXhosa).isChecked = true
        }

        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.radioEnglish -> "en"
                R.id.radioZulu -> "zu"
                R.id.radioXhosa -> "xh"
                else -> "en"
            }
            changeLanguage(languageCode)
        }
    }

    private fun loadSettings() {
        // Load dark mode state
        val isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkModeEnabled
        setDarkMode(isDarkModeEnabled)

        // Load biometric state
        val isBiometricEnabled = sharedPreferences.getBoolean("biometric_enabled", false)
        biometricSwitch.isChecked = isBiometricEnabled
    }

    // Fetch user details from Firestore
    private fun fetchUserDetails() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    userNameTextView.text = document.getString("fullName") ?: getString(R.string.no_name)
                    userEmailTextView.text = document.getString("email") ?: getString(R.string.no_email)

                    storage.reference.child("profileImages/$uid").downloadUrl
                        .addOnSuccessListener { uri ->
                            Glide.with(this).load(uri).into(profileImageView)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, getString(R.string.failed_to_load_profile_image), Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.failed_to_load_user_data), Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Open image picker to upload profile image
    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQUEST)
    }

    // Handle result from image picker and upload the image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            imageUri?.let { uploadProfileImage(it) }
        }
    }

    private fun uploadProfileImage(imageUri: android.net.Uri) {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            val ref = storage.reference.child("profileImages/$uid")
            ref.putFile(imageUri)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.profile_image_uploaded), Toast.LENGTH_SHORT).show()
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this).load(uri).into(profileImageView)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to apply dark mode
    private fun setDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Toast.makeText(this, getString(R.string.dark_mode_enabled), Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Toast.makeText(this, getString(R.string.dark_mode_disabled), Toast.LENGTH_SHORT).show()
        }
    }

    // Biometric authentication setup
    private fun authenticateWithBiometrics() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor: Executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    sharedPreferences.edit().putBoolean("biometric_enabled", true).apply()
                    Toast.makeText(this@SettingsActivity, getString(R.string.biometric_login_enabled), Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@SettingsActivity, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@SettingsActivity, getString(R.string.authentication_error, errString), Toast.LENGTH_SHORT).show()
                    biometricSwitch.isChecked = false
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_authentication))
                .setSubtitle(getString(R.string.enable_biometric_login))
                .setNegativeButtonText(getString(R.string.cancel))
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(this, getString(R.string.biometric_not_supported), Toast.LENGTH_SHORT).show()
            biometricSwitch.isChecked = false
        }
    }

    // Language change function
    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        sharedPreferences.edit().putString("language", languageCode).apply()

        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
