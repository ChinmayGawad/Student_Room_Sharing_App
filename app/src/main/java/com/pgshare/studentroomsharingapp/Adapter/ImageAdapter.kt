package com.pgshare.studentroomsharingapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pgshare.studentroomsharingapp.R

class ImagePagerAdapter(private var imageUrls: List<String>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img_single_room_photo)
    }
    private var imageUrl: MutableList<String?>? = null

    fun setImageUrls(imageUrls: MutableList<String?>?) {
        this.imageUrls = imageUrl as List<String>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageUrls[position]

//         Load the image from Firebase URL into the ImageView
         Glide.with(holder.itemView.context)
             .load(url)
             .placeholder(R.drawable.imageplaceholder)
             .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}
