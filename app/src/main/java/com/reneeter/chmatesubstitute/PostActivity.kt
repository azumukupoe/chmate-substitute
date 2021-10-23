package com.reneeter.chmatesubstitute

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.edit
import androidx.core.view.get
import androidx.preference.PreferenceManager
import com.reneeter.chmatesubstitute.databinding.ActivityPostBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

class PostActivity : AppCompatActivity(R.layout.activity_post) {
    // View Binding
    private val binding by lazy { ActivityPostBinding.bind(findViewById<ViewGroup>(android.R.id.content)[0]) }

    // SharedPreferences
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.postToolbar)

        val useCellular = sharedPreferences.getBoolean("use_cellular", false)

        val anchor = intent.getStringExtra("android.intent.extra.TEXT")
        val originRes = intent.getStringExtra("sourceBody")

        binding.postMessage.setText(anchor)

        var actionMode: ActionMode? = null
        binding.postMessage.setOnLongClickListener {
            if (actionMode == null) {
                actionMode = startSupportActionMode(object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        return if (originRes != null) {
                            mode!!.menuInflater.inflate(R.menu.post_action, menu)
                            true
                        } else {
                            false
                        }
                    }

                    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        return false
                    }

                    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                        return if (item!!.itemId == R.id.origin_quote) {
                            binding.postMessage.append(originRes)
                            mode!!.finish()
                            true
                        } else {
                            false
                        }
                    }

                    override fun onDestroyActionMode(mode: ActionMode?) {
                        actionMode = null
                    }
                })
                true
            } else {
                false
            }
        }

        if (useCellular) {
            binding.postUseCellular.visibility = View.VISIBLE
        }

        binding.postUseCellular.isChecked = sharedPreferences.getBoolean("cellularChecked", false)

        // スレのURL
        val url = intent.dataString!!.removeSuffix("l5")
        // 板・スレッドキー
        val (board, threadKey) =
            Regex("""https://.+\..+\..+/test/read.cgi/(.+)/(.+)/""").find(url)!!.groupValues.drop(1)

        binding.postButton.setOnClickListener {
            binding.group.visibility = View.GONE
            binding.postUseCellular.visibility = View.GONE
            binding.postProgress.visibility = View.VISIBLE

            fun encodeURL(encodeText: Editable): String {
                return URLEncoder.encode(encodeText.toString(), "UTF-8")
            }

            // パラメーターの設定
            val parameters = ("FROM=${encodeURL(binding.postName.text)}" +
                    "&mail=${encodeURL(binding.postEmail.text)}" +
                    "&MESSAGE=${encodeURL(binding.postMessage.text)}" +
                    "&bbs=${board}" +
                    "&key=${threadKey}" +
                    "&time=${(System.currentTimeMillis() / 1000)}" +
                    "&submit=%E6%9B%B8%E3%81%8D%E8%BE%BC%E3%82%80")
                .toRequestBody("application/x-www-form-urlencoded; charset=UTF-8".toMediaType())

            // ヘッダーの設定
            val request = Request.Builder().apply {
                url(url.replace(Regex("""read\.cgi/.+"""), "bbs.cgi"))
                addHeader("Referer", url)
                addHeader("Cookie", "yuki=akari")
                addHeader(
                    "User-Agent",
                    sharedPreferences.getString(
                        "user_agent",
                        getString(R.string.user_agent_default)
                    )!!
                )
                addHeader(
                    "X-2ch-UA",
                    sharedPreferences.getString(
                        "x_2ch_ua",
                        getString(R.string.x_2ch_ua_default)
                    )!!
                )
                post(parameters)
            }.build()

            val callback = PostCallback(board, threadKey, Handler(Looper.getMainLooper()), this)

            if (binding.postUseCellular.isChecked) {
                // セルラー回線を使用
                (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).requestNetwork(
                    NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .build(),
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)

                            // Post
                            OkHttpClient.Builder().socketFactory(network.socketFactory).build()
                                .newCall(request).enqueue(callback)
                        }
                    }
                )
            } else {
                // Post
                OkHttpClient().newCall(request).enqueue(callback)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        sharedPreferences.edit {
            putBoolean("cellularChecked", binding.postUseCellular.isChecked)
        }
    }
}