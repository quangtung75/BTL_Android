package com.qtcoding.btl_android.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {
    private final FirebaseAuth firebaseAuth;

    public AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
