import android.support.v4.app.NotificationCompat;

import com.bunny.gochat.R;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by hp on 24-01-2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.profile_icon)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

    }
}
