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

    private String id;
    private String roomName;
    private String location;
    private String description;
    private String price;
    private String imageUrl;
    private int imageResourceId;

    // Default constructor with no arguments (required by Firebase)
    public Room() {
    }

    public Room(String id, String roomName, String location, String description, String price, String imageUrl, int imageResourceId) {
        this.id = id;
        this.roomName = roomName;
        this.location = location;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageResourceId = imageResourceId;
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
        out.writeString(imageUrl);
        out.writeInt(imageResourceId);
    }

    protected Room(Parcel in) {
        id = in.readString();
        roomName = in.readString();
        location = in.readString();
        description = in.readString();
        price = in.readString();
        imageUrl = in.readString();
        imageResourceId = in.readInt();
    }

    // Getters and setters remain the same

    public void setId(String id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

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
        price = "₹" + price;
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
