package com.pgshare.studentroomsharingapp.Adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.R

class PhotoPreviewAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<PhotoPreviewAdapter.PreviewViewHolder>() {

    class PreviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.img_preview_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_preview, parent, false)
        return PreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        val uri = imageUris[position]
        // Native setImageURI is highly efficient for local device files
        holder.imageView.setImageURI(uri)
    }

    override fun getItemCount(): Int = imageUris.size
}