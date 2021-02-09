package com.example.ece1778;

public class Post {
    private String uID = "";
    private String storageRef = "";
    private String timeStamp = "";

    public Post(){
    }

    public Post(String uID, String storageRef, String timeStamp) {
        this.uID = uID;
        this.storageRef = storageRef;
        this.timeStamp = timeStamp;
    }

    public String getUID() {
        return uID;
    }

    public void setUID(String uID) {
        this.uID = uID;
    }

    public String getStorageRef() {
        return storageRef;
    }

    public void setStorageRef(String storageRef) {
        this.storageRef = storageRef;
    }

    public String getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp(String timestamp) {
        this.timeStamp = timestamp;
    }

}
