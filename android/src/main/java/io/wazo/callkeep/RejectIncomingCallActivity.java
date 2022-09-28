package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALL_UUID;

import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class RejectIncomingCallActivity extends AppCompatActivity {
    public String uuid;
    private static final String TAG = "RNCallKeep";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "[RejectIncomingCallActivity] RejectCallActivity ");
        HashMap<String, String> attributeMap = (HashMap<String, String>)getIntent().getSerializableExtra("attributeMap");

        uuid = attributeMap.get(EXTRA_CALL_UUID);
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn == null) {
            Log.w(TAG, "[RejectIncomingCallActivity] rejectCall ignored because no connection found, uuid: " + uuid);
        } else {
            conn.onReject();
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}
