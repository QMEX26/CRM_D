package com.crm.service;

public interface FirebaseAuthService {
    String createFirebaseUser(String email, String password, String displayName);
    void deleteFirebaseUser(String firebaseUid);
    void updateFirebaseUserPassword(String firebaseUid, String newPassword);
}
