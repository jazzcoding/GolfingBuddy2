package com.golfingbuddy.ui.matches.classes;

import java.util.ArrayList;

/**
 * Created by kairat on 11/17/14.
 */
public class SlideData {
    private int userId;
    private Boolean isAuthorized;
    private Boolean isSubscribe;
    private String authorizeMsg;
    private String displayName;
    private String label;
    private String labelColor;
    private Object compatibility;
    private String location;
    private int ages;
    private Boolean bookmarked;
    private String avatar;
    private ArrayList<String> photos;

    public SlideData() {
        this.photos = new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Boolean getIsAuthorized() {
        return isAuthorized;
    }

    public void setIsAuthorized(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public Boolean getIsSubscribe() {
        return isSubscribe;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
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

    public Boolean isBookmarked() {
        return bookmarked != null && bookmarked == true;
    }

    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ArrayList<String> getPhotos() {
        return this.photos;
    }

    public String getPhoto(int index)
    {
        if (this.photos == null || this.photos.size() == 0)
        {
            return null;
        }
        return this.photos.get(index);
    }

    public void setPhotos(ArrayList<String> photos) {
        for( String photo: photos )
        {
            this.photos.add(photo);
        }
    }

    public void addPhoto( String photo ) {
        this.photos.add(photo);
    }

    public Boolean getIsLimited() {
        return !isAuthorized && avatar != null && !photos.isEmpty() && photos.size() > 1;
    }
}
