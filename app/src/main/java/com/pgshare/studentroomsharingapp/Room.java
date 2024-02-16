package com.pgshare.studentroomsharingapp;

public class Room {

    private  String id,roomName, location, description, price , imageUrl;


    public Room(String id, String roomName, String location, String description, String price, String imageUrl) {
    }

    public Room(String id, String roomName, String location, String description, String price) {
        this.id = id;
        this.roomName = roomName;
        this.location = location;
        this.description = description;
        this.price = price;
    }

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
}
