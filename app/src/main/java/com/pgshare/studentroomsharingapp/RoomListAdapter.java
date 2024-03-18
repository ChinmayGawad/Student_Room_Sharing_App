package com.pgshare.studentroomsharingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    protected List<Room> roomData;
    private final Context context;
    private final OnItemClickListener listener;

    public void setFilteredList(List<Room> filteredList) {
       this.roomData = filteredList;
       notifyDataSetChanged();
    }

    public RoomListAdapter(List<Room> roomData, Context context, OnItemClickListener listener) {
        this.roomData = roomData;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        Room room = roomData.get(position);

        // Bind common fields
        holder.roomTitle.setText(room.getRoomName());
        holder.roomRent.setText(room.getPrice());
        holder.roomLocation.setText(room.getLocation());

        // Load image using Glide
        if (!room.getImageUrls().isEmpty()) {
            String imageUrl = room.getImageUrls().get(0); // Assuming you're loading the first image
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.imageplaceholder)
                    .error(R.drawable.imageplaceholder)
                    .into(holder.roomImage);
        } else {
            // Handle case where there are no image URLs
            holder.roomImage.setImageResource(R.drawable.imageplaceholder);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(room);
            }
        });
    }


    @Override
    public int getItemCount() {
        return roomData.size();
    }

    public void updateData(List<Room> newData) {
        roomData.clear(); // Clear the existing data
        roomData.addAll(newData); // Add the new data to the list
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }


    public interface OnItemClickListener {
        void onItemClick(Room room);
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        private final TextView roomTitle;
        private final TextView roomRent;
        private final ImageView roomImage;
        private final TextView roomLocation;

        public RoomViewHolder(View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.room_title);
            roomRent = itemView.findViewById(R.id.room_rent);
            roomImage = itemView.findViewById(R.id.room_image);
            roomLocation = itemView.findViewById(R.id.roomAddress);
        }
    }
}
