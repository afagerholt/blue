package com.visma.blue.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.visma.blue.BlueMainActivity;
import com.visma.blue.R;

import java.util.Map;

public class BlueFcmListenerService extends FirebaseMessagingService {

    public static final String NOTIFICATION_NEW_DOCUMENT_MESSAGE_TYPE = "PHOTOSERVICE_EMAILED_IMAGES";
    public static final String NOTIFICATION_MESSAGE_KEY = "message";
    public static final String NOTIFICATION_NUMBER_KEY = "msgcnt";

    public static final int NOTIFICATION_ID = 1;

    private static final String TAG = "MyFcmListenerService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map data = message.getData();

        Log.d(TAG, "From: " + from);

        /*
        If this is a notification type of message we end upp here if the app is in the foreground but not if it's in
        the background. For data type of messages we end up here both if the app is in the foreground and background.
        https://firebase.google.com/docs/reference/android/com/google/firebase/messaging/FirebaseMessagingService.html#public-methods
        https://firebase.google.com/docs/cloud-messaging/downstream#sample-receive
        Messages sent from the Firebase Console are notification messages
        */

        RemoteMessage.Notification notification = message.getNotification();
        final String messageKey = (String) data.get(NOTIFICATION_MESSAGE_KEY);
        if (notification != null) {
            createNotification(notification);
        } else if (NOTIFICATION_NEW_DOCUMENT_MESSAGE_TYPE.equals(messageKey)) {
            final int number = Integer.parseInt((String)data.get(NOTIFICATION_NUMBER_KEY));
            createNotification(R.string.visma_blue_icon_title, R.string.visma_blue_notification_content_text, number,
                    R.string.visma_blue_notification_content_text);
        }
    }

    // Creates notification based on title and body received
    private void createNotification(RemoteMessage.Notification notification) {
        Context context = getBaseContext();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.blue_ic_notification_new_document)
                        .setContentTitle(notification.getTitle())
                        .setContentText(notification.getBody())
                        .setColor(ContextCompat.getColor(this, R.color.nc_blue));

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5000, notificationBuilder.build());
    }

    private void createNotification(int titleId, int contentText, int number, int ticker) {
        Boolean sound = true;
        Boolean vibrate = true;
        int defaults = Notification.DEFAULT_LIGHTS;
        if (sound) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (vibrate) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.blue_ic_notification_new_document)
                        .setTicker(getResources().getString(ticker))
                        .setNumber(number)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setContentTitle(getResources().getString(titleId))
                        .setContentText(getResources().getString(contentText))
                        .setDefaults(defaults)
                        .setColor(ContextCompat.getColor(this, R.color.nc_blue));

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, BlueMainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(BlueMainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}