package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALL_UUID;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class AcceptIncomingCallActivity extends AppCompatActivity {
    public String uuid;
    private static final String TAG = "RNCallKeep";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap<String, String> attributeMap = (HashMap<String, String>)getIntent().getSerializableExtra("attributeMap");

        uuid = attributeMap.get(EXTRA_CALL_UUID);

        KeyguardManager keyguardManager = (KeyguardManager)  getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            if(keyguardManager!=null)
                keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
        Log.i(TAG, "[AcceptIncomingCallActivity] AcceptCallActivity ");
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn == null) {
            Log.w(TAG, "[AcceptIncomingCallActivity] answerIncomingCall ignored because no connection found, uuid: " + uuid);
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
