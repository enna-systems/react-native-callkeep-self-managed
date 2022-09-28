package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALLER_NAME;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

public class NotificationService extends Service {
    private static final String TAG = "RNCallKeep";
    private NotificationManager notificationManager;
    private final int random = new Random().nextInt(500);
    private String CHANNEL_ID="care.enna.companionapp" + random;
    private Intent mIntent;
    private Context mContext;
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

        HashMap<String, String> attributeMap = (HashMap<String, String>)intent.getSerializableExtra("attributeMap");

        String imageUrl = attributeMap.get("imageURL");
        Log.i(TAG, "[VoiceConnection] imageURL " + imageUrl);
        //Async Task starten
        new DownloadImageTask(this, intent)
                .execute(imageUrl);

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void createForegroundService(int id, Notification notification) {
        startForeground(id, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Notification Service stopped");
        // Intent intent = new Intent("finish_activity");
        // sendBroadcast(intent);

        //notificationManager.cancel(CHANNEL_ID, random);
        // notificationManager.cancelAll();
        stopForeground(true);
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageTask(Context context, Intent intent) {
            mContext = context;
            mIntent = intent;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                Log.i(TAG, "[NotificationService doInBackground urldisplay] " + urldisplay);
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "[NotificationService Error] " + e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            Log.i(TAG, "[NotificationService setImageBitmap]");

            Bundle extras = new Bundle();
            HashMap<String, String> attributeMap = (HashMap<String, String>)mIntent.getSerializableExtra("attributeMap");
            extras.putSerializable("attributeMap", attributeMap);
            String callerName = attributeMap.get(EXTRA_CALLER_NAME);

            Intent rejectIntent = new Intent(mContext, RejectIncomingCallActivity.class);
            rejectIntent.putExtra("rejectCall",true);
            rejectIntent.putExtras(extras);
            //rejectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingRejectIntent = PendingIntent.getActivity(mContext, 1,
                    rejectIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent acceptIntent = new Intent(mContext, AcceptIncomingCallActivity.class);
            acceptIntent.putExtra("acceptCall",true);
            acceptIntent.putExtras(extras);
            //acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingAcceptIntent = PendingIntent.getActivity(mContext, 2,
                    acceptIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent fullScreenIntent = new Intent(mContext, IncomingCallActivity.class);
            fullScreenIntent.putExtras(extras);
            fullScreenIntent.setAction(Long.toString(System.currentTimeMillis()));
            fullScreenIntent.setFlags( Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            //fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mContext, 3,
                    fullScreenIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

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
                    new Notification.Builder(mContext, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_round)
                            //.setLargeIcon(result)
                            .setContentTitle("enna Videocall")
                            .setContentText(callerName)
                            //.setPriority(Notification.PRIORITY_HIGH)
                            .setCategory(Notification.CATEGORY_CALL)
                            //.setDefaults(Notification.DEFAULT_LIGHTS)
                            .setAutoCancel(true)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setOngoing(true)
                            //.setSound(Settings.System.DEFAULT_RINGTONE_URI)
                            //.setVibrate(new long[]{250, 250, 250, 250, 1000})
                            //.setContentIntent(fullScreenPendingIntent)
                            // Use a full-screen intent only for the highest-priority alerts where you
                            // have an associated activity that you would like to launch after the user
                            // interacts with the notification. Also, if your app targets Android 10
                            // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                            // order for the platform to invoke this notification.
                            .setFullScreenIntent(fullScreenPendingIntent, true);

            //UI
            RemoteViews customView = new RemoteViews(mContext.getPackageName(), R.layout.custom_call_notification);
            RemoteViews customFullScreenView = new RemoteViews(mContext.getPackageName(), R.layout.activity_incoming_call);

            customView.setTextViewText(R.id.name, callerName);
            customView.setImageViewBitmap(R.id.photo, result);
            customView.setOnClickPendingIntent(R.id.btnAnswer, pendingAcceptIntent);
            customView.setOnClickPendingIntent(R.id.btnDecline, pendingRejectIntent);

            notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
            notificationBuilder.setCustomContentView(customView);
            // notificationBuilder.setCustomBigContentView(customView);


/*
        Notification.Action acceptAction = new Notification.Action.Builder(R.drawable.ic_call_accept, "Accept", pendingAcceptIntent).build();
        Notification.Action declineAction = new Notification.Action.Builder(R.drawable.ic_call_reject, "Decline", pendingRejectIntent).build();
        notificationBuilder.addAction(acceptAction);
        notificationBuilder.addAction(declineAction);

 */


            //}
            notificationBuilder.setChannelId(CHANNEL_ID);
            Notification notification = notificationBuilder.build();
            // notificationManager.notify(CHANNEL_ID, random, notification);
            createForegroundService(random, notification);
            //startForeground(random, notification);
            //cancel(true);

        }
    }
}
