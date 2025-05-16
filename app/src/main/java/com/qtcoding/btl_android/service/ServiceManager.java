package com.qtcoding.btl_android.service;

public class ServiceManager {
    //Singleton
    private static ServiceManager instance;

    // List of services

    private ServiceManager() {

    }

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    // Add methods to get services
}
