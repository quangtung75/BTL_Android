package com.qtcoding.btl_android.service;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qtcoding.btl_android.model.User;

public class UserService {
    private final FirebaseFirestore firestore;

    public UserService() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void createUser(User user, ServiceCallback<Void> callback) {
        firestore.collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getUser(String userId, ServiceCallback<User> callback) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUser(User user, ServiceCallback<Void> callback) {
        firestore.collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void countCollectionsByUser(String userId, ServiceCallback<Long> callback) {
        firestore.collection("vocabCollections")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess((long) querySnapshot.size()))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateStudyDuration(String userId, int studyDuration, ServiceCallback<Void> callback) {
        firestore.collection("users")
                .document(userId)
                .update("studyDurationInMinutes", studyDuration)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateNotificationTime(String userId, int hour, int minute, ServiceCallback<Void> callback) {
        firestore.collection("users")
                .document(userId)
                .update(
                        "notificationHour", hour,
                        "notificationMinute", minute
                )
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }


}
