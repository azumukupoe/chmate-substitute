package com.reneeter.chmatesubstitute

import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mikepenz.aboutlibraries.LibsBuilder
import com.reneeter.chmatesubstitute.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        val binding = ActivitySettingsBinding.bind(findViewById<ViewGroup>(android.R.id.content)[0])

        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setTitle(R.string.settings_title)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        // 権限取得Launcher
        private val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                val context = requireContext()
                if (uri == null) {
                    Toast.makeText(
                        context,
                        R.string.permission_error_toast_text,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }

                val contentResolver = context.contentResolver
                val uriPermissions = contentResolver.persistedUriPermissions
                val requestPermission =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                if (uriPermissions.size != 0) {
                    contentResolver.releasePersistableUriPermission(
                        uriPermissions[0].uri,
                        requestPermission
                    )
                }

                contentResolver.takePersistableUriPermission(uri, requestPermission)
            }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // 権限取得
            findPreference<Preference>("get_permission")?.setOnPreferenceClickListener {
                permissionLauncher.launch(
                    DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents",
                        "primary:Android/data/jp.co.airfront.android.a2chMate/files/2chMate/dat"
                    )
                )
                true
            }

            // About画面
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                LibsBuilder().apply {
                    aboutShowIcon = true
                    aboutAppName = getString(R.string.app_name)
                    aboutShowVersionName = true
                    aboutDescription = getString(R.string.about_description)
                    start(requireContext())
                }
                true
            }
        }
    }
}