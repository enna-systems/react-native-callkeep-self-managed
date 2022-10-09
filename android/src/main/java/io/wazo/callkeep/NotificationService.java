package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALLER_NAME;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

public class NotificationService extends Service {
    private static final String TAG = "RNCallKeep";
    private final int random = new Random().nextInt(500);
    private final String CHANNEL_ID="care.enna.companionapp" + random;
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

        stopForeground(true);
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private final String filename = "ennaProfilePicture.png";

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

                FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
                mIcon11.compress(Bitmap.CompressFormat.PNG, 100, stream);

                //Cleanup
                stream.close();
            } catch (Exception e) {
                Log.e(TAG, "[NotificationService Error] " + e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        public Bitmap getCircleBitmap(Bitmap bitmap) {
            Bitmap output;
            Rect srcRect, dstRect;
            float r;
            final int width = bitmap.getWidth();
            final int height = bitmap.getHeight();

            if (width > height){
                output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
                int left = (width - height) / 2;
                int right = left + height;
                srcRect = new Rect(left, 0, right, height);
                dstRect = new Rect(0, 0, height, height);
                r = height / 2;
            }else{
                output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
                int top = (height - width)/2;
                int bottom = top + width;
                srcRect = new Rect(0, top, width, bottom);
                dstRect = new Rect(0, 0, width, width);
                r = width / 2;
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(r, r, r, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

            bitmap.recycle();

            return output;
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
            //rejectIntent.putExtra("rejectCall",true);
            rejectIntent.putExtras(extras);
            //rejectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingRejectIntent = PendingIntent.getBroadcast(mContext, 1,
                    rejectIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent acceptIntent = new Intent(mContext, AcceptIncomingCallActivity.class);
            //acceptIntent.putExtra("acceptCall",true);
            acceptIntent.putExtras(extras);
            //acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingAcceptIntent = PendingIntent.getActivity(mContext, 2,
                    acceptIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent fullScreenIntent = new Intent(mContext, IncomingCallActivity.class);
            fullScreenIntent.putExtras(extras);
            fullScreenIntent.putExtra("profilePicture", filename);

            fullScreenIntent.setAction(Long.toString(System.currentTimeMillis()));
            fullScreenIntent.setFlags( Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            //fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mContext, 3,
                    fullScreenIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

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

                            .setContentTitle(getResources().getString(R.string.incoming_call))
                            .setContentText(getResources().getString(R.string.incoming_call))
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
                Notification.Action acceptAction = new Notification.Action.Builder(Icon.createWithResource(mContext, R.drawable.ic_answer), getResources().getString(R.string.answer), pendingAcceptIntent).build();
                Notification.Action declineAction = new Notification.Action.Builder(Icon.createWithResource(mContext, R.drawable.ic_reject), getResources().getString(R.string.decline), pendingRejectIntent).build();
                notificationBuilder.addAction(acceptAction);
                notificationBuilder.addAction(declineAction);
                notificationBuilder.setContentTitle(callerName);
            }

            //Custom Push UI

            //RemoteViews customView = new RemoteViews(mContext.getPackageName(), R.layout.custom_call_notification);
            //customView.setTextViewText(R.id.name, callerName);
            //customView.setOnClickPendingIntent(R.id.btnAnswer, pendingAcceptIntent);
            //customView.setOnClickPendingIntent(R.id.btnDecline, pendingRejectIntent);

            //notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
            //notificationBuilder.setCustomContentView(customView);
            //notificationBuilder.setCustomBigContentView(customView);

            if(result != null) {
                //customView.setImageViewBitmap(R.id.photo, result);
                Bitmap roundedBitmap = getCircleBitmap(result);
                notificationBuilder.setLargeIcon(roundedBitmap);

            } else {
                //set enna icon
                //customView.setImageViewResource(R.id.photo, R.drawable.ic_launcher_round);
                notificationBuilder.setLargeIcon(Icon.createWithResource(mContext, R.drawable.ic_launcher_round));
            }

            notificationBuilder.setChannelId(CHANNEL_ID);
            Notification notification = notificationBuilder.build();
            createForegroundService(random, notification);
        }
    }
}
