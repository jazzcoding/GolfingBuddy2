package com.golfingbuddy.model.base.classes;

import java.util.Map;

/**
 * Created by sardar on 1/16/15.
 */
public class SkUser {
    private int userId;
    private String displayName;
    private String avatarUrl;
    private String bigAvatarUrl;
    private String origAvatarUrl;
    private String email;
    private Boolean isSuspended;
    private Boolean isApproved;
    private Boolean isEmailVerified;
    private Boolean isAvatarApproved;
    private String username;
    private int age;
    private int sex;
    private Map<String, Integer> birthday;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public String getBigAvatarUrl() {
        return bigAvatarUrl;
    }

    public void setBigAvatarUrl(String bigAvatarUrl) {
        this.bigAvatarUrl = bigAvatarUrl;
    }

    public String getOrigAvatarUrl() {
        return origAvatarUrl;
    }

    public void setOrigAvatarUrl(String origAvatarUrl) {
        this.origAvatarUrl = origAvatarUrl;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public Boolean getIsAvatarApproved() {
        return isAvatarApproved;
    }

    public void setIsAvatarApproved(Boolean isAvatarApproved) {
        this.isAvatarApproved = isAvatarApproved;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public boolean hasBirthday() {
        return birthday != null &&
                birthday.containsKey("year") &&
                birthday.containsKey("month") &&
                birthday.containsKey("day");
    }

    public Map<String, Integer> getBirthday() {
        return birthday;
    }

    public void setBirthday(Map<String, Integer> birthday) {
        this.birthday = birthday;
    }
}
