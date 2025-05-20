package com.qtcoding.btl_android.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {
    //sd để tương tác Auth của Firebase
    private final FirebaseAuth firebaseAuth;

    //khởi tạo AuthService
    public AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Lấy ng dùng hiện tại
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void signUp(String email, String password, ServiceCallback<Void> callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null); // Vì không trả về dữ liệu cụ thể
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void login(String email, String password, ServiceCallback<Void> callback){
        //Gọi API firebase để đăng nhập
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    callback.onSuccess(null);
                }
                else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    public void logout(ServiceCallback<Void> callback){
        firebaseAuth.signOut();
        callback.onSuccess(null);
    }
}
