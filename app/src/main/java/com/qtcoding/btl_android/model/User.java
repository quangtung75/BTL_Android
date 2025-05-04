package com.qtcoding.btl_android.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private int notificationHour;
    private int notificationMinute;
    private int studyDurationInMinutes;

    public User() {
    }

    public User(String id, String name, String email, String photoUrl, int notificationHour, int notificationMinute, int studyDurationInMinutes) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.notificationHour = notificationHour;
        this.notificationMinute = notificationMinute;
        this.studyDurationInMinutes = studyDurationInMinutes;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getNotificationHour() {
        return notificationHour;
    }

    public void setNotificationHour(int notificationHour) {
        this.notificationHour = notificationHour;
    }

    public int getNotificationMinute() {
        return notificationMinute;
    }

    public void setNotificationMinute(int notificationMinute) {
        this.notificationMinute = notificationMinute;
    }

    public int getStudyDurationInMinutes() {
        return studyDurationInMinutes;
    }

    public void setStudyDurationInMinutes(int studyDurationInMinutes) {
        this.studyDurationInMinutes = studyDurationInMinutes;
    }
}
