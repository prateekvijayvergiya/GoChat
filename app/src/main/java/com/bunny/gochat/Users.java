package com.bunny.gochat;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by hp on 13-01-2018.
 */

public class Users {

    public String name;
    public String image;
    public String status;
    public String thumbImage;

    public Users(){

    }



    public Users(String name, String image, String status, String thumbImage) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumbImage = thumbImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    /**
     * Created by hp on 24-01-2018.
     */

    public static class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.profile_icon)
                            .setContentTitle("Friend Request")
                            .setContentText("You've Received a new Friend Request");


            int mNotificationId = (int) System.currentTimeMillis();
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}
