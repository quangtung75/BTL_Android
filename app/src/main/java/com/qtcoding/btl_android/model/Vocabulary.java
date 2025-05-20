package com.qtcoding.btl_android.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Vocabulary implements Parcelable {
    private String id;
    private String word;
    private String meaning;
    private String example;
    private String collectionId;

    public Vocabulary() {
    }

    public Vocabulary(String id, String word, String meaning, String example, String collectionId) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.collectionId = collectionId;
    }

    //Sử dụng để truyền dữ liệu giữa các thành phần trong ứng dụng Android (F t F)
    protected Vocabulary(Parcel in) {
        id = in.readString();
        word = in.readString();
        meaning = in.readString();
        example = in.readString();
        collectionId = in.readString();
    }

    //Ghi  thuộc tính của đối tượng vào Parcel để truyền đi
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(word);
        dest.writeString(meaning);
        dest.writeString(example);
        dest.writeString(collectionId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //đối tượng tĩnh để tạo đối tượng Vocabulary từ Parcel
    public static final Creator<Vocabulary> CREATOR = new Creator<Vocabulary>() {
        @Override
        public Vocabulary createFromParcel(Parcel in) {
            return new Vocabulary(in);
        }

        @Override
        public Vocabulary[] newArray(int size) {
            return new Vocabulary[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }


    @Override
    public String toString() {
        return "Vocabulary{" +
                "id='" + id + '\'' +
                ", word='" + word + '\'' +
                ", meaning='" + meaning + '\'' +
                ", example='" + example + '\'' +
                ", collectionId='" + collectionId + '\'' +
                '}';
    }
    //Chuyển đổi Vocabulary thành Map để lưu vào Firestore.
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("word", word);
        result.put("meaning", meaning);
        result.put("example", example);
        result.put("collectionId", collectionId);
        return result;
    }
}