package com.pgshare.studentroomsharingapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter.RoomViewHolder
import com.pgshare.studentroomsharingapp.R



class RoomListAdapter(
    protected var roomData: MutableList<Room>,
    private val context: Context,
    private val listener: OnItemClickListener?
) : RecyclerView.Adapter<RoomViewHolder?>() {
    fun setFilteredList(filteredList: MutableList<Room>) {
        this.roomData = filteredList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): RoomViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_room, p0, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomData.get(position)

        // Bind common fields
        holder.roomTitle.setText(room.roomName)
        holder.roomRent.setText(room.formatPrice)
        holder.roomLocation.setText(room.location)

        // Load image using Glide
        if (!room.imageUrls!!.isEmpty()) {
            val imageUrl = room.imageUrls!!.get(0) // Assuming you're loading the first image
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.imageplaceholder)
                .error(R.drawable.imageplaceholder)
                .into(holder.roomImage)
        } else {
            // Handle case where there are no image URLs
            holder.roomImage.setImageResource(R.drawable.imageplaceholder)
        }

        // Set click listener
        holder.itemView.setOnClickListener { v: View? ->
            if (listener != null) {
                listener.onItemClick(room)
            }
        }
    }


    override fun getItemCount(): Int {
        return roomData.size
    }

    fun updateData(newData: MutableList<Room?>) {
        roomData.clear() // Clear the existing data
        roomData.addAll(newData as Collection<Room>) // Add the new data to the list
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }


    interface OnItemClickListener {
        fun onItemClick(room: Room?)
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomTitle: TextView
        val roomRent: TextView
        val roomImage: ImageView
        val roomLocation: TextView

        init {
            roomTitle = itemView.findViewById<TextView>(R.id.room_title)
            roomRent = itemView.findViewById<TextView>(R.id.room_rent)
            roomImage = itemView.findViewById<ImageView>(R.id.room_image)
            roomLocation = itemView.findViewById<TextView>(R.id.roomAddress)
        }
    }
}
