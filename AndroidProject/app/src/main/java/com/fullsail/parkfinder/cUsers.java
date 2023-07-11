package com.fullsail.parkfinder;

public class cUsers {

    public String username, imgURL, uid, fullname, request_type;





    public cUsers(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public String setUid(String uid) {
        this.uid = uid;
        return uid;
    }

    public void setRequestType(String requestType) {
        this.request_type = requestType;
    }

    public String getRequestType() {
        return request_type;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImage() {
        return imgURL;
    }

    public void setImage(String image) {
        this.imgURL = image;
    }
}
