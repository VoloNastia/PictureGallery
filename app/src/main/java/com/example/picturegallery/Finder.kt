package com.example.picturegallery

import android.content.Context
import android.widget.ImageView
import java.io.File
import android.graphics.BitmapFactory



object Finder {
    fun getFilenameFromURL(url: String?): String {
        return "file" + url?.hashCode().toString()
    }

    fun setImageIntoView(context: Context?, url: String?, view: ImageView): Boolean {
        val existingFiles = context?.fileList()
        val filename = getFilenameFromURL(url)
        return if (existingFiles != null && existingFiles.contains(filename)) {
            val bitmap = BitmapFactory.decodeFile(File(context.filesDir, filename).absolutePath)
            view.setImageBitmap(bitmap)
            true
        } else {
            false
        }
    }
}