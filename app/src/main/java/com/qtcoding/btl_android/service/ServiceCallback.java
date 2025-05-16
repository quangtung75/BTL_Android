package com.qtcoding.btl_android.service;

public interface ServiceCallback<T> {
    void onSuccess(T result);

    void onFailure(Exception e);
}