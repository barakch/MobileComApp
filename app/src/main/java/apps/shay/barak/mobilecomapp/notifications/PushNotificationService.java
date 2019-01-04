package apps.shay.barak.mobilecomapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import apps.shay.barak.mobilecomapp.R;
import apps.shay.barak.mobilecomapp.activities.MainActivity;

public class PushNotificationService extends FirebaseMessagingService {

    private static final String TAG ="PushNotificationService";
    public static final String PUSH_ACTION = "push_action";
    public static final int ACTION_DISCOUNT=2, ACTION_SHARE=3, ACTION_OPEN=4;

    public PushNotificationService() {
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "onMessageReceived() >>");
        String title = "title";
        String body = "body";
        int icon = R.drawable.ic_notifications_black_24dp;
        Uri soundRri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Map<String,String> data;
        RemoteMessage.Notification notification;


        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: " + remoteMessage.getFrom());


        if (remoteMessage.getNotification() == null) {
            Log.e(TAG, "onMessageReceived() >> Notification is empty");
        } else {
            notification = remoteMessage.getNotification();
            title = notification.getTitle();
            body = notification.getBody();
            Log.e(TAG, "onMessageReceived() >> title: " + title + " , body="+body);
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() == 0) {
            Log.e(TAG, "onMessageReceived() << No data doing nothing");
            return;
        }


        //parse the data
        data = remoteMessage.getData();
        Log.e(TAG, "Message data : " + data);

        String value = data.get("title");
        if (value != null) {
            title = value;
        }

        value = data.get("body");
        if (value != null) {
            body = value;
        }

        value = data.get("small_icon");
        if (value != null  && value.equals("alarm")) {
            icon = R.drawable.ic_alarm_black_24dp;
        }
        value = data.get("sound");
        if (value != null) {
            if (value.equals("alert")) {
                soundRri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            } else if (value.equals("ringtone")) {
                soundRri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String channelId = "fcm_default_channel";

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, null)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(icon)
                        .setSound(soundRri)
                        .setChannelId(channelId);


        value = data.get("action");
        if (value != null) {
            if (value.contains("share")) {
                intent.putExtra(PUSH_ACTION, ACTION_SHARE);
                Log.d(TAG, "Got share notification");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out notflix at: https://play.google.com/store/apps/details?id=apps.shay.barak.mobilecomapp");
                sendIntent.setType("text/plain");
                PendingIntent pendingShareIntent = PendingIntent.getActivity(this, 0 , sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_shopping_cart_black_24dp,"Let's invite them!",pendingShareIntent));
                notificationBuilder.setContentIntent(pendingShareIntent);
            }
            if(value.contains("open")){
                PendingIntent pendingShareIntent = PendingIntent.getActivity(this, 0 , intent,
                        PendingIntent.FLAG_ONE_SHOT);
                notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_shopping_cart_black_24dp,"Go to Notflix",pendingShareIntent));
            }
            if(value.contains("discount")){
                intent.putExtra(PUSH_ACTION, ACTION_DISCOUNT);
                intent.putExtra("series_key", data.get("series_key"));
                intent.putExtra("percentage", Float.parseFloat(data.get("percentage")));
                Log.d(TAG, "Got test notification");
                Log.d(TAG, data.get("series_key"));
                Log.d(TAG,  ""+Float.parseFloat(data.get("percentage")));
                PendingIntent pendingShareIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_shopping_cart_black_24dp,"Go to sale!",pendingShareIntent));
                notificationBuilder.setContentIntent(pendingShareIntent);

            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(channelId,
                    "Default",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 , notificationBuilder.build());

        Log.e(TAG, "onMessageReceived() <<");


    }
}
