package com.example.picturegallery

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.picturegallery.dummy.PicturesContent
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*

class ItemDetailFragment : Fragment() {
    private var item: PicturesContent.PictureItem? = null
    private lateinit var view: ImageView
    lateinit var receiver: ServiceReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = ServiceReceiver(Handler())
        receiver.setReceiver(object : ServiceReceiver.Receiver {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (resultCode == RESULT_OK) {
                    val resultValue = resultData.getByteArray("RESULT_VALUE")
                    val bitmap = BitmapFactory.decodeByteArray(resultValue, 0, resultValue.size)
                    view.setImageBitmap(bitmap)
                    val filename = resultData.getString("FILENAME")
                    context?.openFileOutput(filename, Context.MODE_PRIVATE).use {
                        it?.write(resultValue)
                    }
                }
            }
        })
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                item = PicturesContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_detail, container, false)
        view = rootView.item_detail
        if (!Finder.setImageIntoView(context, item?.download_link, view)) {
            val intent = Intent(context, Loader::class.java).apply {
                putExtra("EXTRA_URL", item?.download_link)
                putExtra("RECEIVER", receiver)
            }
            context?.startService(intent)
        }
        return rootView
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}