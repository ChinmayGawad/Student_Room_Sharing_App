package com.pgshare.studentroomsharingapp.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.R

class ImagePagerAdapter(private val imageUrls: List<String?>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Ensure this ID matches the ImageView inside your item_image.xml
        val imageView: ImageView = view.findViewById(R.id.img_single_room_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val base64String = imageUrls[position]

        if (!base64String.isNullOrEmpty()) {
            try {
                // Convert the Base64 string back into raw bytes
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                // Compile bytes into a renderable Bitmap image
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback safe asset if decoding fails safely
                holder.imageView.setImageResource(R.drawable.imageplaceholder)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.imageplaceholder)
        }
    }

    override fun getItemCount(): Int = imageUrls.size
}