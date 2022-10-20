package io.wazo.callkeep;

import static io.wazo.callkeep.Constants.EXTRA_CALL_UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.Connection;
import android.util.Log;

import java.util.HashMap;

public class RejectIncomingCallActivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        HashMap<String, String> attributeMap = (HashMap<String, String>)intent.getSerializableExtra("attributeMap");

        String uuid = attributeMap.get(EXTRA_CALL_UUID);
        Connection conn = VoiceConnectionService.getConnection(uuid);
        if (conn != null) {
            conn.onReject();
        }
    }
}
