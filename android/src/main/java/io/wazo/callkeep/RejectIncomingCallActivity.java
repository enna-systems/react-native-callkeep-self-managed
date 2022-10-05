package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALL_UUID;

import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class RejectIncomingCallActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap<String, String> attributeMap = (HashMap<String, String>)getIntent().getSerializableExtra("attributeMap");

        String uuid = attributeMap.get(EXTRA_CALL_UUID);
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn != null) {
            conn.onReject();
        }

        finishAndRemoveTask();
    }
}
