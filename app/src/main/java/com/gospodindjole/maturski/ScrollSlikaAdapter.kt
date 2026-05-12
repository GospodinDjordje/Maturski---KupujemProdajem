package com.gospodindjole.maturski

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class ScrollSlikaAdapter(
    private val slike: List<String>
) : RecyclerView.Adapter<ScrollSlikaAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.scrollSlika)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scroll_slika, parent, false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val stream = URL(slike[position]).openStream()
            val bitmap = BitmapFactory.decodeStream(stream)

            withContext(Dispatchers.Main) {
                holder.imageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun getItemCount(): Int {
        return slike.size
    }
}