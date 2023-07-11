package com.fullsail.parkfinder;

import java.io.Serializable;

public class cAddress implements Serializable {
    public String postalCode, city, state, line1, line2, line3, type;

    public cAddress() {
        this.postalCode = "";
        this.city = "";
        this.state = "";
        this.line1 = "";
        this.line2 = "";
        this.line3 = "";
        this.type = "";
    }

    public cAddress(String postalCode, String city, String state, String line1, String line2, String line3, String type) {
        this.postalCode = postalCode;
        this.city = city;
        this.state = state;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.type = type;
    }
}
