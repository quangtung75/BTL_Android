package com.qtcoding.btl_android.model;

public class UserCollectionFollow {
    private String userId;
    private String collectionId;

    public UserCollectionFollow() {
    }

    public UserCollectionFollow(String userId, String collectionId) {
        this.userId = userId;
        this.collectionId = collectionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
}
