package com.pgshare.studentroomsharingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    private final List<Room> roomData;
    private final Context context;
    private final OnItemClickListener listener;

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

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        Room room = roomData.get(position);

        // Bind common fields (ensure these fields exist in your Room class)
        holder.roomTitle.setText(room.getRoomName());
        holder.roomRent.setText(context.getString(R.string.rent_format, room.getPrice()));
        holder.roomDescription.setText(room.getDescription());

       /* // Bind additional fields (replace with your actual field names and view IDs)
        if (room.hasAddress()) { // Assuming address is an optional field
            holder.roomAddress.setText(room.getAddress());
        } else {
            // Hide or handle the address view if address is not available
        }

        if (room.hasAmenities()) { // Assuming amenities is a list of strings
            holder.roomAmenities.setText(room.getAmenitiesAsString()); // Use a suitable method to format amenities
        } else {
            // Hide or handle the amenities view if amenities are not available
        }

        // ... (bind any other fields as needed)

        // Set image (replace with your image loading logic)
        // Glide or Picasso can be used for efficient image loading
        // holder.roomImage.setImageURI(...);*/

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
        private final TextView roomDescription;
        private final ImageView roomImage;

        public RoomViewHolder(View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.room_title);
            roomRent = itemView.findViewById(R.id.room_rent);
            roomDescription = itemView.findViewById(R.id.room_description);
            roomImage = itemView.findViewById(R.id.room_image);
        }
    }
}
