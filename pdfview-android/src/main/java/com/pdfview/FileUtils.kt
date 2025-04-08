package com.pdfview

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

object FileUtils {

    @Throws(IOException::class)
    fun fileFromAsset(context: Context, assetFileName: String): File {
        val outFile = File(context.cacheDir, "$assetFileName-pdfview.pdf")
        if (assetFileName.contains("/")) {
            outFile.parentFile?.mkdirs()
        }
        context.assets.open(assetFileName).copyTo(outFile.outputStream())
        return outFile
    }

    @Throws(IOException::class)
    fun fileFromUri(context: Context, uri: Uri): File {
        val outFile = File(context.cacheDir, "uri-pdfview.pdf")
        val openInputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream")
        openInputStream.copyTo(outFile.outputStream())
        return outFile
    }
}
