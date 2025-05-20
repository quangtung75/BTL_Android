package com.qtcoding.btl_android.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.qtcoding.btl_android.model.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class VocabularyService {
    private FirebaseFirestore db;

    public VocabularyService() {
        db = FirebaseFirestore.getInstance();
    }

    // Thêm từ vựng mới
    public void addVocabulary(Vocabulary vocabulary, ServiceCallback<Void> callback) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                // 1. Tăng cardCount của bộ sưu tập
                DocumentReference collectionRef = db.collection("vocabCollections").document(vocabulary.getCollectionId());
                DocumentSnapshot snapshot = transaction.get(collectionRef);
                Long currentCount = snapshot.getLong("cardCount");
                if (currentCount == null) currentCount = 0L;
                transaction.update(collectionRef, "cardCount", currentCount + 1);

                // 2. Thêm từ vựng
                DocumentReference vocabRef = db.collection("vocabularies").document(vocabulary.getId());
                transaction.set(vocabRef, vocabulary.toMap()); // Sử dụng toMap()

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void updateVocabulary(Vocabulary vocabulary, ServiceCallback<Void> callback) {
        db.collection("vocabularies").document(vocabulary.getId())
                .set(vocabulary.toMap(), SetOptions.merge()) // Sử dụng toMap()
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

    // Xóa từ vựng
    public void deleteVocabulary(Vocabulary vocabulary, ServiceCallback<Void> callback) {
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        // 1. Giảm cardCount
                        DocumentReference collectionRef = db.collection("vocabCollections").document(vocabulary.getCollectionId());
                        DocumentSnapshot snapshot = transaction.get(collectionRef);
                        Long currentCount = snapshot.getLong("cardCount");
                        if (currentCount == null || currentCount <= 0) currentCount = 1L;

                        // 2. Xóa từ vựng
                        DocumentReference vocabRef = db.collection("vocabularies").document(vocabulary.getId());
                        transaction.delete(vocabRef);

                        transaction.update(collectionRef, "cardCount", currentCount - 1);

                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void getVocabulariesByCollectionId(String collectionId, ServiceCallback<List<Vocabulary>> callback) {
        db.collection("vocabularies")
                .whereEqualTo("collectionId", collectionId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Vocabulary> result = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Vocabulary vocab = doc.toObject(Vocabulary.class);
                        if (vocab != null) {
                            result.add(vocab);
                        }
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
