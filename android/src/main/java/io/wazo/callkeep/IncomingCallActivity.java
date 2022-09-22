package io.wazo.callkeep;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telecom.Connection;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import static io.wazo.callkeep.Constants.EXTRA_CALLER_NAME;
import static io.wazo.callkeep.Constants.EXTRA_CALL_NUMBER;
import static io.wazo.callkeep.Constants.EXTRA_CALL_UUID;

public class IncomingCallActivity extends AppCompatActivity {

    public String uuid, callerName = "";

    private static final String TAG = "RNCallKeep";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap<String, String> attributeMap = (HashMap<String, String>)getIntent().getSerializableExtra("attributeMap");

        uuid = attributeMap.get(EXTRA_CALL_UUID);
        callerName = attributeMap.get(EXTRA_CALLER_NAME);
        Log.i(TAG,"[IncomingCallActivity] uuid " + uuid + " callerName " + callerName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager)  getSystemService(Context.KEYGUARD_SERVICE);
            if(keyguardManager!=null)
                keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
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
                onAcceptClicked();
            }
        });
        stopService(new Intent(this, NotificationService.class));
    }

    private void onRejectClicked() {
        Log.i("[IncomingCallActivity] Call rejected");
        //new RNCallKeepModule().stopRingtone();
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn == null) {
            Log.w(TAG, "[IncomingCallActivity] rejectCall ignored because no connection found, uuid: " + uuid);
        } else {
            conn.onReject();
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    private void onAcceptClicked() {
        Log.i(TAG,"[IncomingCallActivity] Call accepted");
        //VoiceConnection.stopRingtone();
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn == null) {
            Log.w(TAG, "[IncomingCallActivity] answerIncomingCall ignored because no connection found, uuid: " + uuid);
        } else {
            conn.onAnswer();
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}