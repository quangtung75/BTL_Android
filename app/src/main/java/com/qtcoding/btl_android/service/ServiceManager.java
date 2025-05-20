package com.qtcoding.btl_android.service;

public class ServiceManager {
    //Singleton
    private static ServiceManager instance;

    // List of services
    private final AuthService authService;

    private final UserService userService;

    private final VocabCollectionService vocabCollectionService;
    private final VocabularyService vocabularyService;


    private ServiceManager() {

        authService = new AuthService();
        userService = new UserService();
        vocabCollectionService = new VocabCollectionService();
        vocabularyService = new VocabularyService();
    }

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    // khai báo các phương thức để lấy services
    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }
    public VocabCollectionService getVocabCollectionService() {
        return vocabCollectionService;
    }

    public VocabularyService getVocabularyService() {
        return vocabularyService;
    }
}
