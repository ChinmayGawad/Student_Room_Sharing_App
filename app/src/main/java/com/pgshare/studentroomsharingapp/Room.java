package com.pgshare.studentroomsharingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Room implements Parcelable {

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    private String id, roomName, location, description, price, imageUrl;

    // Default constructor with no arguments (required by Firebase)
    public Room() {
    }

    public Room(String id, String roomName, String location, String description, String price, String imageUrl) {
        this.id = id;
        this.roomName = roomName;
        this.location = location;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Room(String id, String roomName, String location, String description, String price) {
        this(id, roomName, location, description, price, null); // Assign null to imageUrl for the simpler constructor
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(roomName);
        out.writeString(location);
        out.writeString(description);
        out.writeString(price);
        out.writeString(imageUrl); // Include imageUrl if necessary
    }

    protected Room(Parcel in) {
        id = in.readString();
        roomName = in.readString();
        location = in.readString();
        description = in.readString();
        price = in.readString();
        imageUrl = in.readString(); // Read imageUrl if necessary
    }

    // Getters and setters remain the same

    public String getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
