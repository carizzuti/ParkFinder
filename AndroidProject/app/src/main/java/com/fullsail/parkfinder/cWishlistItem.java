package com.fullsail.parkfinder;

import java.io.Serializable;

public class cWishlistItem implements Serializable {
    String parkName, states, notes, todoList, key, parkcode, imageURL;

    public cWishlistItem(String parkName, String states, String notes, String todoList, String key, String parkcode, String imageURL) {
        this.parkName = parkName;
        this.states = states;
        this.notes = notes;
        this.todoList = todoList;
        this.key = key;
        this.parkcode = parkcode;
        this.imageURL = imageURL;
    }

    public cWishlistItem() {
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTodoList(String todoList) {
        this.todoList = todoList;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setParkcode(String parkcode) {
        this.parkcode = parkcode;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getParkName() {
        return parkName;
    }

    public String getStates() {
        return states;
    }

    public String getNotes() {
        return notes;
    }

    public String getTodoList() {
        return todoList;
    }

    public String getKey() {
        return key;
    }

    public String getParkcode() {
        return parkcode;
    }

    public String getImageURL() {
        return imageURL;
    }
}
