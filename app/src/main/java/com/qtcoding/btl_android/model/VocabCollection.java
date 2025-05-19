package com.qtcoding.btl_android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

public class VocabCollection implements Parcelable {

    private String id;
    private String name;
    private String description;
    private String ownerId;
    private int followerCount;
    private int cardCount;

    // Các trường không lưu trong Firebase nhưng cần cho UI
    @Exclude
    private boolean isOwned;      // Người dùng hiện tại có sở hữu không
    @Exclude
    private boolean isFollowing;  // Người dùng hiện tại có đang follow không

    // Constructor mặc định
    public VocabCollection() {
    }

    // Constructor đầy đủ
    public VocabCollection(String id, String name, String description, String ownerId, int followerCount, int cardCount, boolean isOwned, boolean isFollowing) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.followerCount = followerCount;
        this.cardCount = cardCount;
        this.isOwned = isOwned;
        this.isFollowing = isFollowing;
    }

    // Các getter và setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    @Exclude
    public boolean isOwned() {
        return isOwned;
    }

    public void setOwned(boolean owned) {
        isOwned = owned;
    }

    @Exclude
    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Implement Parcelable methods
    protected VocabCollection(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        ownerId = in.readString();
        followerCount = in.readInt();
        cardCount = in.readInt();
        isOwned = in.readByte() != 0;
        isFollowing = in.readByte() != 0;
    }

    public static final Creator<VocabCollection> CREATOR = new Creator<VocabCollection>() {
        @Override
        public VocabCollection createFromParcel(Parcel in) {
            return new VocabCollection(in);
        }

        @Override
        public VocabCollection[] newArray(int size) {
            return new VocabCollection[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(ownerId);
        dest.writeInt(followerCount);
        dest.writeInt(cardCount);
        dest.writeByte((byte) (isOwned ? 1 : 0));
        dest.writeByte((byte) (isFollowing ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    public String getStability() {
        return "Some value";
    }

    @Override
    public String toString() {
        return "VocabCollection{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", followerCount=" + followerCount +
                ", cardCount=" + cardCount +
                ", isOwned=" + isOwned +
                ", isFollowing=" + isFollowing +
                '}';
    }
}
