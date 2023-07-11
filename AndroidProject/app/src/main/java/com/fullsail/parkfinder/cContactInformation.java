package com.fullsail.parkfinder;

import java.io.Serializable;
import java.util.List;

public class cContactInformation implements Serializable {
    private List<cPhoneNumber> phoneNumbers;
    private List<cAddress> addresses;
    private String email;

    public cContactInformation(List<cPhoneNumber> phoneNumbers, List<cAddress> addresses, String email) {
        this.phoneNumbers = phoneNumbers;
        this.addresses = addresses;
        this.email = email;
    }

    public void setPhoneNumbers(List<cPhoneNumber>phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void setAddresses(List<cAddress> addresses) {
        this.addresses = addresses;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<cPhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<cAddress> getAddresses() {
        return addresses;
    }

    public String getEmail() {
        return email;
    }
}
