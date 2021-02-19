package com.example.ece1778;

public class Comment {
    private String uID = "";
    private String username = "";
    private String displayPicPath = "";
    private String commentContent = "";
    private String timeStamp = "";

    public Comment(){
    }

    public Comment(String uID, String username, String displayPicPath, String commentContent, String timeStamp) {
        this.uID = uID;
        this.username = username;
        this.displayPicPath = displayPicPath;
        this.commentContent = commentContent;
        this.timeStamp = timeStamp;
    }

    //Getter and Setter
    public String getUID() {
        return uID;
    }

    public void setUID(String uID) {
        this.uID = uID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayPicPath() {
        return displayPicPath;
    }

    public void setDisplayPicPath(String displayPicPath) {
        this.displayPicPath = displayPicPath;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
