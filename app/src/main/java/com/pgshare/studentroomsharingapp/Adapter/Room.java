package com.pgshare.studentroomsharingapp.Adapter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

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
    private ArrayList<String> imageUrls = new ArrayList<>();
    private int imageResourceId;

    // Default constructor with no arguments (required by Firebase)
    public Room() {
    }

    public Room(String id, String roomName, String location, String description, String price, ArrayList<String> imageUrls, int imageResourceId) {
        this.id = id;
        this.roomName = roomName;
        this.location = location;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;
        this.imageResourceId = imageResourceId;
    }

    protected Room(Parcel in) {
        id = in.readString();
        roomName = in.readString();
        location = in.readString();
        description = in.readString();
        price = in.readString();
        imageUrls = new ArrayList<>(); // Initialize the ArrayList
        in.readStringList(imageUrls);
        imageResourceId = in.readInt();
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
        out.writeStringList(imageUrls);
        out.writeInt(imageResourceId);
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFormatPrice() {
        // Format price to display with two decimal places and currency symbol
        return "₹" + getPrice();
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
}
