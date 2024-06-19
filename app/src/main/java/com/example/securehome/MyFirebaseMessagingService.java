package com.example.securehome;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here
        if (remoteMessage.getData().size() > 0) {
            // Handle data messages
        }

        if (remoteMessage.getNotification() != null) {
            // Handle notification messages
            // Display notification or perform any other action
        }
    }

    @Override
    public void onNewToken(String token) {
        // Called when a new token is generated or refreshed
        // You can send the token to your server here
    }
}
