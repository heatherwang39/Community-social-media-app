package com.example.ece1778;

public class Comment {
    private String username = "";
    private String displayPicPath = "";
    private String commentContent = "";
    private String timeStamp = "";
    private String postURL = "";

    public Comment(){
    }

    public Comment(String username, String displayPicPath, String commentContent, String timeStamp, String postURL) {
        this.username = username;
        this.displayPicPath = displayPicPath;
        this.commentContent = commentContent;
        this.timeStamp = timeStamp;
        this.postURL = postURL;
    }

    //Getter and Setter
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


    public String getPostURL() { return postURL; }

    public void setPostURL(String postURL) { this.postURL = postURL; }

}
