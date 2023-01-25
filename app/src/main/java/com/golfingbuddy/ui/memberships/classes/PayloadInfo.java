package com.golfingbuddy.ui.memberships.classes;

/**
 * Created by jk on 3/26/15.
 */
public class PayloadInfo {
    String userId;
    String productId;
    String hash;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
