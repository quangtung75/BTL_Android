package com.qtcoding.btl_android.service;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.qtcoding.btl_android.model.VocabCollection;

import java.util.ArrayList;
import java.util.List;

public class VocabCollectionService {
    private final FirebaseFirestore db;

    public VocabCollectionService() {
        db = FirebaseFirestore.getInstance();
    }
    public void getTopCollections(String currentUserId, ServiceCallback<List<VocabCollection>> callback) {
        db.collection("vocabCollections")
                .orderBy("followerCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VocabCollection> collections = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        VocabCollection collection = doc.toObject(VocabCollection.class);
                        if (collection != null) {
                            collection.setOwned(collection.getOwnerId().equals(currentUserId));
                            collections.add(collection);
                        }
                    }
                    // Kiểm tra trạng thái follow cho tất cả collections
                    List<String> collectionIds = new ArrayList<>();
                    for (VocabCollection collection : collections) {
                        collectionIds.add(collection.getId());
                    }
                    if (collectionIds.isEmpty()) {
                        callback.onSuccess(collections);
                        return;
                    }
                    db.collection("userCollectionFollows")
                            .whereEqualTo("userId", currentUserId)
                            .whereIn("collectionId", collectionIds)
                            .get()
                            .addOnSuccessListener(followSnapshots -> {
                                List<String> followedIds = new ArrayList<>();
                                for (var doc : followSnapshots) {
                                    String collectionId = doc.getString("collectionId");
                                    if (collectionId != null) {
                                        followedIds.add(collectionId);
                                    }
                                }
                                for (VocabCollection collection : collections) {
                                    collection.setFollowing(followedIds.contains(collection.getId()));
                                }
                                callback.onSuccess(collections);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
