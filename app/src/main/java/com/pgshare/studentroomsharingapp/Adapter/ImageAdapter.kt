package com.pgshare.studentroomsharingapp.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.R

class ImagePagerAdapter(private val base64Images: List<String>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ensure this ID matches the ImageView in your item_image.xml
        val imageView: ImageView = itemView.findViewById(R.id.img_single_room_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val base64String = base64Images[position]

        try {
            // Decode the Base64 string back into a byte array, then to a Bitmap
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            holder.imageView.setImageBitmap(decodedImage)
            holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP // Ensures it fills the ViewPager nicely

        } catch (e: Exception) {
            Log.e("ImagePagerAdapter", "Failed to decode Base64 image", e)
            // Fallback to placeholder if decoding fails
            holder.imageView.setImageResource(R.drawable.imageplaceholder)
        }
    }

    override fun getItemCount(): Int {
        return base64Images.size
    }
}