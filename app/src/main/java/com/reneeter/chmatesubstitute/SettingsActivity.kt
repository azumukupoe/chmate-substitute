package com.reneeter.chmatesubstitute

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

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