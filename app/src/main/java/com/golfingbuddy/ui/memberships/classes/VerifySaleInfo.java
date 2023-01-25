package com.golfingbuddy.ui.memberships.classes;

/**
 * Created by jk on 3/26/15.
 */
public class VerifySaleInfo {
    String registered;
    String error;
    String isValid;

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        this.registered = registered;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}