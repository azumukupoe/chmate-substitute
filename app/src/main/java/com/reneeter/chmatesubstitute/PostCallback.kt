package com.reneeter.chmatesubstitute

import android.app.Activity
import android.os.Handler
import android.provider.DocumentsContract
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class PostCallback(
    private val board: String,
    private val threadKey: String,
    private val handler: Handler,
    private val activity: Activity
) : Callback {
    private fun success(response: Response) {
        // Postに失敗したか
        val resNum = response.header("x-Resnum")
        if (resNum == null) failure()

        // idx書き込み
        val uriPermissions = activity.contentResolver.persistedUriPermissions
        if (uriPermissions.size == 0) failure()

        val contentResolver = activity.contentResolver
        val uri = uriPermissions[0].uri
        val streamUri = DocumentsContract.buildDocumentUriUsingTree(
            uri, "${DocumentsContract.getTreeDocumentId(uri)}/${board}_${threadKey}.idx"
        )

        val idxText = contentResolver.openInputStream(streamUri)!!.bufferedReader().use { reader ->
            reader.readLines()
        }.toMutableList()
        idxText[1] = "${if (idxText[1].isNotEmpty()) "${idxText[1]}," else ""}${resNum}:1"

        contentResolver.openOutputStream(streamUri)!!.bufferedWriter().use { writer ->
            writer.write(idxText.joinToString("\n"))
        }

        handler.post {
            Toast.makeText(
                activity,
                R.string.post_success_toast_text,
                Toast.LENGTH_SHORT
            ).show()
        }
        activity.finish()
    }

    private fun failure() {
        handler.post {
            Toast.makeText(
                activity,
                R.string.post_failed_toast_text,
                Toast.LENGTH_SHORT
            ).show()
        }
        activity.finish()
    }

    override fun onResponse(call: Call, response: Response) = success(response)

    override fun onFailure(call: Call, e: IOException) = failure()
}