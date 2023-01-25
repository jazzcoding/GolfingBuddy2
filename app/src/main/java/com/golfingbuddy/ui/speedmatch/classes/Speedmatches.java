package com.golfingbuddy.ui.speedmatch.classes;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by kairat on 12/8/14.
 */
public class Speedmatches {
    private int userId;
    private Boolean isAuthorized;
    private Boolean isSubscribe;
    private String authorizeMsg;
    private Boolean isOnline;
    private String displayName;
    private String location;
    private int ages;
    private String avatar;
    private ArrayList<String> photos;
    private Object compatibility;
    private ArrayList<Info> info;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Boolean getIsAuthorized() {
        return isAuthorized == true;
    }

    public void setIsAuthorized(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public Boolean getIsSubscribe() {
        return isSubscribe == true;
    }

    public void setIsSubscribe(Boolean isSubscribe) {
        this.isSubscribe = isSubscribe;
    }

    public String getAuthorizeMsg() {
        return authorizeMsg;
    }

    public void setAuthorizeMsg(String authorizeMsg) {
        this.authorizeMsg = authorizeMsg;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getAges() {
        return ages;
    }

    public void setAges(int ages) {
        this.ages = ages;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public String getPhoto(int position) {
        return photos.get(position);
    }

    public void addPhoto(String photo) {
        this.photos.add(photo);
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }

    public Object getRealCompatibility() {
        return compatibility;
    }

    public int getCompatibility() {
        try {
            if (compatibility instanceof Double) {
                Double result = (Double) compatibility;
                return result.intValue();
            } else {
                Integer result = (Integer) compatibility;
                return result;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public void setCompatibility(Object compatibility) {
        this.compatibility = compatibility;
    }

    public ArrayList<Info> getInfo() {
        return info;
    }

    public void setInfo(ArrayList<Info> info) {
        this.info = info;
    }

    public class Info {
        private String label;
        private Object value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            if (value == null) {
                return "";
            }
            else if (value instanceof ArrayList) {
                return TextUtils.join(", ", (ArrayList) value);
            } else {
                return value.toString();
            }
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
