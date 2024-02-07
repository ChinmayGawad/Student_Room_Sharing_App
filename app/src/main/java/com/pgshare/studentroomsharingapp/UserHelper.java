package com.pgshare.studentroomsharingapp;

public class UserHelper {

    private String name,email,phoneNo,aadhar,gender;

    public UserHelper() {
    }

    public UserHelper(String name, String email, String phoneNo, String aadhar, String gender) {
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.aadhar = aadhar;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

