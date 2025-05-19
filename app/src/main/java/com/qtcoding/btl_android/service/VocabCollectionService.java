package com.qtcoding.btl_android.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.qtcoding.btl_android.model.UserOwnedCollection;
import com.qtcoding.btl_android.model.VocabCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // Tìm kiếm VocabCollection theo tên
    public void searchCollectionsByName(String query, String currentUserId, ServiceCallback<List<VocabCollection>> callback) {
        db.collection("vocabCollections")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
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

    public void toggleFollow(String currentUserId, String collectionId, boolean isFollowing, ServiceCallback<Void> callback) {
        DocumentReference followRef = db.collection("userCollectionFollows")
                .document(currentUserId + "_" + collectionId);
        DocumentReference collectionRef = db.collection("vocabCollections")
                .document(collectionId);

        db.runTransaction(transaction -> {
                    if (isFollowing) {
                        // Hủy theo dõi
                        transaction.delete(followRef);
                        transaction.update(collectionRef, "followerCount", FieldValue.increment(-1));
                    } else {
                        // Theo dõi
                        Map<String, Object> followData = new HashMap<>();
                        followData.put("userId", currentUserId);
                        followData.put("collectionId", collectionId);
                        transaction.set(followRef, followData);
                        transaction.update(collectionRef, "followerCount", FieldValue.increment(1));
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // Lấy tất cả VocabCollection mà người dùng sở hữu
    public void getOwnedCollections(String currentUserId, ServiceCallback<List<VocabCollection>> callback) {
        db.collection("userOwnedCollections")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> collectionIds = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        String collectionId = doc.getString("collectionId");
                        if (collectionId != null) {
                            collectionIds.add(collectionId);
                        }
                    }
                    if (collectionIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    db.collection("vocabCollections")
                            .whereIn("id", collectionIds)
                            .get()
                            .addOnSuccessListener(collectionSnapshots -> {
                                List<VocabCollection> collections = new ArrayList<>();
                                for (var doc : collectionSnapshots) {
                                    VocabCollection collection = doc.toObject(VocabCollection.class);
                                    if (collection != null) {
                                        collection.setOwned(true);
                                        collections.add(collection);
                                    }
                                }
                                // Kiểm tra trạng thái follow
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
                })
                .addOnFailureListener(callback::onFailure);
    }
    // Lấy tất cả VocabCollection mà người dùng follow
    public void getFollowedCollections(String currentUserId, ServiceCallback<List<VocabCollection>> callback) {
        db.collection("userCollectionFollows")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> collectionIds = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        String collectionId = doc.getString("collectionId");
                        if (collectionId != null) {
                            collectionIds.add(collectionId);
                        }
                    }
                    if (collectionIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    db.collection("vocabCollections")
                            .whereIn("id", collectionIds)
                            .get()
                            .addOnSuccessListener(collectionSnapshots -> {
                                List<VocabCollection> collections = new ArrayList<>();
                                for (var doc : collectionSnapshots) {
                                    VocabCollection collection = doc.toObject(VocabCollection.class);
                                    if (collection != null) {
                                        collection.setOwned(collection.getOwnerId().equals(currentUserId));
                                        collection.setFollowing(true);
                                        collections.add(collection);
                                    }
                                }
                                callback.onSuccess(collections);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    //cac ham crud
    public void addCollection(String currentUserId, VocabCollection collection, ServiceCallback<Void> callback){
        collection.setId(UUID.randomUUID().toString());
        collection.setOwnerId(currentUserId);
        collection.setFollowerCount(0);
        collection.setCardCount(0);
        collection.setOwned(true); //set ng tao collection la chu
        collection.setFollowing(false); //do moi tao nen chua co ai follow

        db.collection("vocabCollections").document(collection.getId()).set(collection).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //tao document id va obj roi ghi vao collection
                String documentId = currentUserId + "_" + collection.getId();
                UserOwnedCollection userOwnedCollection = new UserOwnedCollection(currentUserId, collection.getId());
                db.collection("userOwnedCollections").document(documentId).set(userOwnedCollection)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //thanh cong
                                callback.onSuccess(null);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //ghi userOwnedCollection ko dc
                                callback.onFailure(e);
                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //luu vocabCollection fail
                        callback.onFailure(e);
                    }
                });
    }
    public void updateCollection(VocabCollection collection, ServiceCallback<Void> callback){
        db.collection("vocabCollections").document(collection.getId())
                .update("name", collection.getName(), "description", collection.getDescription())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void deleteCollection(String currentUserId, String collectionId, ServiceCallback<Void> callback){
        //xoa khoi collection
        db.collection("vocabCollections").document(collectionId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //xoa khoi userCollection
                        db.collection("userOwnedCollections").document(currentUserId + "_" + collectionId)
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //xoa het vocab lien quan
                                        db.collection("vocabularies").whereEqualTo("collectionId", collectionId)
                                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for (var doc : queryDocumentSnapshots) {
                                                            doc.getReference().delete();
                                                        }
                                                        // xoa het follow trong userCollectionFollows
                                                        db.collection("userCollectionFollows").whereEqualTo("collectionId", collectionId)
                                                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(QuerySnapshot followSnapshots) {
                                                                        for (var doc : followSnapshots) {
                                                                            doc.getReference().delete();
                                                                        }
                                                                        callback.onSuccess(null);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        callback.onFailure(e);
                                                                    }
                                                                });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        callback.onFailure(e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }
}
