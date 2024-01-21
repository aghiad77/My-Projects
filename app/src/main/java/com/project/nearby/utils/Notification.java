package com.project.nearby.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.project.nearby.R;

public class Notification {
    Context context;
    public Notification(Context context) {
        this.context = context;
    }

    public void buildNotification(String content){
        String CHANNEL_ID = "my_channel_01";
        int notifyID = 1;
        android.app.Notification notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle("Social Alert")
                .setSmallIcon(R.drawable.sociallogo)
                .setAutoCancel(true)
                .setContentText(content).build();
        NotificationManager mNotificationManager =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Issue the notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationManager.notify(notifyID , notification);

    }
}
