package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALLER_NAME;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Random;

public class NotificationService extends Service {
    private static final String TAG = "RNCallKeep";
    private NotificationManager notificationManager;
    private final int random = new Random().nextInt(500);
    private String CHANNEL_ID="care.enna.companionapp" + random;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Notification");
        super.onStartCommand(intent, flags, startId);

        Bundle extras = new Bundle();
        HashMap<String, String> attributeMap = (HashMap<String, String>)intent.getSerializableExtra("attributeMap");
        extras.putSerializable("attributeMap", attributeMap);
        String callerName = attributeMap.get(EXTRA_CALLER_NAME);

        Intent rejectIntent = new Intent(this, IncomingCallActivity.class);
        rejectIntent.putExtra("rejectCall",true);
        rejectIntent.putExtras(extras);
        PendingIntent pendingRejectIntent = PendingIntent.getActivity(this, 1,
                rejectIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent acceptIntent = new Intent(this, IncomingCallActivity.class);
        acceptIntent.putExtra("acceptCall",true);
        acceptIntent.putExtras(extras);
        PendingIntent pendingAcceptIntent = PendingIntent.getActivity(this, 2,
                acceptIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent fullScreenIntent = new Intent(this, IncomingCallActivity.class);
        fullScreenIntent.putExtras(extras);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 3,
                fullScreenIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);



        Uri soundUri =  RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        //For API 26+ you need to put some additional code like below:
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, "enna", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.RED);
            mChannel.enableLights(true);
            mChannel.enableVibration(false);
            // mChannel.setVibrationPattern(new long[]{250, 250, 250, 250, 1000});
            mChannel.setDescription("enna Notification");
           /* AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            */
            mChannel.setSound(null, null);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel( mChannel );
            }
        }

        Notification.Builder notificationBuilder =
                new Notification.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_round)
                        .setContentTitle("enna Videocall")
                        .setContentText(callerName)
                        //.setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_CALL)
                        //.setDefaults(Notification.DEFAULT_LIGHTS)
                        .setAutoCancel(true)
                        //.setOngoing(true)
                        //.setSound(Settings.System.DEFAULT_RINGTONE_URI)
                        //.setVibrate(new long[]{250, 250, 250, 250, 1000})
                        .setContentIntent(fullScreenPendingIntent)
                        // Use a full-screen intent only for the highest-priority alerts where you
                        // have an associated activity that you would like to launch after the user
                        // interacts with the notification. Also, if your app targets Android 10
                        // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                        // order for the platform to invoke this notification.
                        .setFullScreenIntent(fullScreenPendingIntent, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Create a new call with the user as caller.
            Person incoming_caller = new Person.Builder()
                    .setName(callerName)
                    .setImportant(true)
                    .build();

            notificationBuilder.setStyle(
                    Notification.CallStyle.forIncomingCall(incoming_caller, pendingRejectIntent, pendingAcceptIntent));
        }
        else {
            Notification.Action acceptAction = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_launcher_round), "Accept", pendingAcceptIntent).build();
            Notification.Action declineAction = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_launcher_round), "Decline", pendingRejectIntent).build();
            notificationBuilder.addAction(acceptAction);
            notificationBuilder.addAction(declineAction);
        }
        notificationBuilder.setChannelId(CHANNEL_ID);
        Notification notification = notificationBuilder.build();
        startForeground(random, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Notification Service stopped");
        stopForeground(true);
    }
}
