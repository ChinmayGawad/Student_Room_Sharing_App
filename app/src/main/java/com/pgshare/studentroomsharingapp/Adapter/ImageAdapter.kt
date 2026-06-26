package com.pgshare.studentroomsharingapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pgshare.studentroomsharingapp.Adapter.ImageAdapter.ImageViewHolder
import com.pgshare.studentroomsharingapp.R

class ImageAdapter : RecyclerView.Adapter<ImageViewHolder?>() {
    private var imageUrls: MutableList<String?>? = null

    fun setImageUrls(imageUrls: MutableList<String?>?) {
        this.imageUrls = imageUrls
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls!!.get(position)
        Glide.with(holder.itemView.getContext())
            .load(imageUrl)
            .placeholder(R.drawable.imageplaceholder)
            .error(R.drawable.imageplaceholder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return if (imageUrls != null) imageUrls!!.size else 0
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView

        init {
            imageView = itemView.findViewById<ImageView>(R.id.imageView)
        }
    }
}
