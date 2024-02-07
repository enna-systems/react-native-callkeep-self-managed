package io.wazo.callkeep;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.util.HashMap;

import static io.wazo.callkeep.Constants.EXTRA_CALLER_NAME;
import static io.wazo.callkeep.Constants.EXTRA_CALL_UUID;

public class IncomingCallActivity extends AppCompatActivity {

    public String uuid, callerName = "";
    private IncomingBroadcastReceiver incomingBroadcastReceiver;
    private Activity mActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        HashMap<String, String> attributeMap = (HashMap<String, String>)getIntent().getSerializableExtra("attributeMap");

        uuid = attributeMap.get(EXTRA_CALL_UUID);
        callerName = attributeMap.get(EXTRA_CALLER_NAME);
        String imageUrl = attributeMap.get("imageURL");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_incoming_call);
        findViewById(R.id.btn_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRejectClicked();
            }
        });
        findViewById(R.id.btn_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyguardManager keyguardManager = (KeyguardManager)  getSystemService(Context.KEYGUARD_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    if(keyguardManager != null)
                        keyguardManager.requestDismissKeyguard(mActivity, null);
                }
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                onAcceptClicked();
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.avatar);
        TextView textView = (TextView) findViewById(R.id.txt_caller_name);

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("profilePicture");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(bmp != null && imageUrl != null && !imageUrl.isEmpty()) {
            imageView.setImageBitmap(bmp);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_round);
        }
        textView.setText(callerName);

        incomingBroadcastReceiver = new IncomingBroadcastReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(incomingBroadcastReceiver, new IntentFilter("finish_activity"), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(incomingBroadcastReceiver, new IntentFilter("finish_activity"));
        }
        stopService(new Intent(this, NotificationService.class));
    }

    private void onRejectClicked() {
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn != null) {
            conn.onReject();
        }
        finishAndRemoveTask();
    }

    private void onAcceptClicked() {
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn != null) {
            conn.onAnswer();
        }
        finishAndRemoveTask();
    }

    private class IncomingBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                finishAndRemoveTask();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(incomingBroadcastReceiver);
    }
}