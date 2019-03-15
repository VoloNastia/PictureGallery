package com.example.picturegallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.example.picturegallery.dummy.PicturesContent
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*

class ItemListActivity : AppCompatActivity() {

    private var twoPane: Boolean = false
    lateinit var receiver: ServiceReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)
        setSupportActionBar(toolbar)
        toolbar.title = title

        if (item_detail_container != null) {
            twoPane = true
        }
        receiver = ServiceReceiver(Handler())
        receiver.setReceiver(object : ServiceReceiver.Receiver {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (resultCode == Activity.RESULT_OK) {
                    val resultValue = resultData.getByteArray("RESULT_VALUE")
                    PicturesContent.parse(resultValue)
                    setupRecyclerView(item_list)
                }
            }
        })
        if (PicturesContent.ITEMS.size == 0) {
            PicturesContent.loadJson(this, receiver)
        } else {
            setupRecyclerView(item_list)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerViewAdapter(this, PicturesContent.ITEMS, twoPane)
    }

    class RecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val values: List<PicturesContent.PictureItem>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as PicturesContent.PictureItem
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.description)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.description)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(values[position])
            with(holder.itemView) {
                tag = values[position]
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private var receiver: ServiceReceiver = ServiceReceiver(Handler())

            init {
                receiver.setReceiver(object : ServiceReceiver.Receiver {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                        if (resultCode == Activity.RESULT_OK) {
                            val resultValue = resultData.getByteArray("RESULT_VALUE")
                            imageView.setImageBitmap(
                                BitmapFactory.decodeByteArray(resultValue, 0, resultValue.size)
                            )
                            val filename = resultData.getString("FILENAME")
                            parentActivity.openFileOutput(filename, Context.MODE_PRIVATE).use {
                                it?.write(resultValue)
                            }
                        }
                    }
                })
            }

            fun bind(item: PicturesContent.PictureItem) {
                textView.text = item.description
                if (!Finder.setImageIntoView(parentActivity, item.preview, imageView)) {
                    Loader.load(parentActivity, item.preview, receiver)
                }
            }

            private val textView: TextView = view.content
            val imageView: ImageView = view.itemImageView
        }
    }
}