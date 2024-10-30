package com.example.rideshare.noti;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.rideshare.R;
import com.example.rideshare.activity.DriverRideDetailActivity;
import com.example.rideshare.activity.MainActivity;
import com.example.rideshare.activity.RideDetailActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
        if (remoteMessage.getNotification() != null) {
            // Get the notification details
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Extract custom data
            String rideId = remoteMessage.getData().get("rideId");
            String bookingId = remoteMessage.getData().get("bookingId");

            // Depending on the type of notification, launch different activities
            Intent intent = null;
            if (bookingId != null) {
                intent = new Intent(this, DriverRideDetailActivity.class);
                intent.putExtra("bookingId", bookingId);
            } else if (rideId != null) {
                intent = new Intent(this, RideDetailActivity.class);
                intent.putExtra("rideId", rideId);
            } else {
                intent = new Intent(this, MainActivity.class); // Default intent
            }

            // Show a notification
            sendNotification(title, body, intent);
        }
    }

    private void sendNotification(String title, String messageBody, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "ride_notifications";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_info)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since Android Oreo, you need to create a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
