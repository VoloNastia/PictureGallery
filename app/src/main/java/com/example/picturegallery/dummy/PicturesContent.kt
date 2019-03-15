package com.example.picturegallery.dummy

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.HashMap
import com.example.picturegallery.Constants
import com.example.picturegallery.Loader
import com.example.picturegallery.ServiceReceiver
import com.google.gson.stream.JsonReader
import java.io.InputStreamReader

object PicturesContent {

    val ITEMS: MutableList<PictureItem> = ArrayList()
    val ITEM_MAP: MutableMap<String, PictureItem> = HashMap()
    fun loadJson(context: Context, receiver: ServiceReceiver) {
        Loader.load(
            context,
            "https://api.unsplash.com/search/photos/?query=${Constants.REQUEST}&per_page=${Constants.COUNT_OF_ITEMS}&client_id=${Constants.KEY}",
            receiver
        )
    }

    fun parse(data: ByteArray?) {
        val str = JsonParser().parse(JsonReader(InputStreamReader(data?.inputStream()))) as JsonObject
        val array = str.getAsJsonArray("results")
        for (i in 0 until array.size()) {
            if (!array[i].asJsonObject.get("description").isJsonNull) {
                val description = array[i].asJsonObject.get("description").asString
                val download = array[i].asJsonObject.getAsJsonObject("urls").get("regular").asString
                val preview = array[i].asJsonObject.getAsJsonObject("urls").get("thumb").asString
                addItem(PictureItem(description, download, preview))
            }
        }
    }

    private fun addItem(item: PictureItem) {
        ITEMS.add(item)
        ITEM_MAP[item.description] = item
    }

    data class PictureItem(val description: String, val download_link: String, val preview: String)

}