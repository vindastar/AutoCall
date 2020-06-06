package com.mm.autocall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OutGoingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取播出的去电号码
        String outPhone = getResultData();
        Log.i("TAG", "收到拨号拨出的广播,号码为:" + outPhone);
    }
}
