package com.pgshare.studentroomsharingapp.Adapter;

public class Owner {

    private String ownerId, ownerName, ownerEmail, ownerPhone, userType,imageUrl, gender;

    public Owner(String ownerId, String ownerName, String ownerPhone, String gender, String imageUrl, String userType) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.userType = userType;
        this.gender = gender;
        this.imageUrl = imageUrl;
    }

    public Owner(String ownerId, String ownerEmail) {
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
    }


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }
}
