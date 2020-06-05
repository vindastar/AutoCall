package com.mm.autocall;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TelListener extends PhoneStateListener {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        Log.i("TelephoneState", "TelephoneState state = " + state);


        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE: // 空闲状态，即无来电也无去电
                Log.i("TelephoneState", "IDLE");
                //此处添加一系列功能代码
                break;
            case TelephonyManager.CALL_STATE_RINGING: // 来电响铃
                Log.i("TelephoneState", "RINGING");
                //此处添加一系列功能代码
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机，即接通
                Log.i("TelephoneState", "OFFHOOK");
                //此处添加一系列功能代码
                break;
        }

//        Log.i("TelephoneState", String.valueOf(incomingNumber));
    }
}
