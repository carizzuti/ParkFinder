package com.fullsail.parkfinder;

import java.io.Serializable;

public class cPhoneNumber implements Serializable {
    public String phoneNumber, type;

    public cPhoneNumber() {
        this.phoneNumber = "";
        this.type = "";
    }

    public cPhoneNumber(String phoneNumber, String type) {
        this.phoneNumber = phoneNumber;
        this.type = type;
    }
}
