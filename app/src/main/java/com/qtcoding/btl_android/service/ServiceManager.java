package com.qtcoding.btl_android.service;

public class ServiceManager {
    //Singleton
    private static ServiceManager instance;

    // List of services
    private final AuthService authService;

    private final UserService userService;

    private ServiceManager() {

        authService = new AuthService();
        userService = new UserService();
    }

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    // Add methods to get services
    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }
}
