package com.mm.autocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText et_phoneStartNum, et_callPhoneCount, et_waitSecond, et_callSpace;
    Button btn_start, btn_pause, btn_check;
    TextView textViewLog;

    boolean isRun;
    int curCount;
    int callCount;
    int waitSecondSec;
    int callSpace;

    Context context;
    String curPhoneNum;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d("TAG", "收到handler, obj = " + msg.obj);

            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    endCall();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isRun) {
                                if (curCount < callCount) {
                                    curCount++;
                                    curPhoneNum = (Long.parseLong(curPhoneNum) + 1) + "";
                                    startWork(curPhoneNum, waitSecondSec);
                                } else {
                                    curCount = 0;
                                    textViewLog.setText(textViewLog.getText() + "\n大哥打完了");
                                }
                            } else {
                                //textViewLog.setText(textViewLog.getText() + "\n暂停了，当前是第: " + curCount + "\n");
                            }
                        }
                    }, callSpace * 1000);
                    break;
                case 2:
                    startWork(curPhoneNum, waitSecondSec);
                    break;
                case 3:
                    textViewLog.setText("快去自如等待租房子!");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doGet("https://sc.ftqq.com/SCU91479T874dbeeafc0118246217ec541e4cad175e7ee288d33e2.send?text=快去自如等待租房子~\n" + formatTime());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                case 4:
                    textViewLog.setText(formatTime() + ": 找房第  " + zhaofangCount + "  次");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewLog.setText(formatTime() + ": 找房第  " + zhaofangCount + "  次");
                            btn_check.callOnClick();
                        }
                    }, 350 * 1000);
            }
        }
    };

    public String doGet(String URL) {
        HttpURLConnection conn = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder result = new StringBuilder();
        try {
            //创建远程url连接对象
            URL url = new URL(URL);
            //通过远程url连接对象打开一个连接，强转成HTTPURLConnection类
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //设置连接超时时间和读取超时时间
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Accept", "application/json");
            //发送请求
            conn.connect();
            //通过conn取得输入流，并使用Reader读取
            if (200 == conn.getResponseCode()) {
                is = conn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                    System.out.println(line);
                }
            } else {
                System.out.println("ResponseCode is an error code:" + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            conn.disconnect();
        }
        return result.toString();
    }

    private void endCall() {
        try {
            // 首先拿到TelephonyManager
            TelephonyManager telMag = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Class<TelephonyManager> c = TelephonyManager.class;

            // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
            //允许访问私有方法
            mthEndCall.setAccessible(true);
            final Object obj = mthEndCall.invoke(telMag, (Object[]) null);

            // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
            Method mt = obj.getClass().getMethod("endCall");
            //允许访问私有方法
            mt.setAccessible(true);
            mt.invoke(obj);
            Log.d("TAG", "挂断电话！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        et_phoneStartNum = (EditText) findViewById(R.id.startPhoneNum);
        et_callPhoneCount = (EditText) findViewById(R.id.callPhoneCount);
        et_waitSecond = (EditText) findViewById(R.id.waitSecond);
        et_callSpace = (EditText) findViewById(R.id.callSpace);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_check = (Button) findViewById(R.id.btn_check);
        textViewLog = (TextView) findViewById(R.id.textViewLog);
        /**
         * 在activity 或者 service中加入如下代码，以实现来电状态监听
         */
        TelephonyManager telMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telMgr.listen(new TelListener(), PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        OutGoingCallReceiver mInnerOutCallReceiver = new OutGoingCallReceiver();
        registerReceiver(mInnerOutCallReceiver, intentFilter);

        btn_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isRun = true;
                btn_start.setEnabled(false);
                btn_pause.setEnabled(true);
                getAvailableSimCardCount(context);
                curPhoneNum = et_phoneStartNum.getText().toString();
                callCount = Integer.parseInt(et_callPhoneCount.getText().toString());
                waitSecondSec = Integer.parseInt(et_waitSecond.getText().toString());
                callSpace = Integer.parseInt(et_callSpace.getText().toString());
                startWork(curPhoneNum, waitSecondSec);
            }
        });

        btn_pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isRun = false;
                btn_start.setEnabled(true);
                btn_pause.setEnabled(false);
                textViewLog.setText(textViewLog.getText() + "\n暂停了，当前是第: " + curCount + "\n----------↑");
            }
        });


        btn_check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String s = getWebContent("http://sh.ziroom.com/x/807488012.html");
                        Log.d("TAG", "XXXXXX s . length() " + s.length());
                        if (s.contains("检测中") || s.length() < 500) {
                            Log.d("TAG", "XXXXXXXXXXXXX");
                            handler.sendEmptyMessage(4);
                        } else {
                            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                            Ringtone rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
                            rt.play();
                            handler.sendEmptyMessage(3);
                        }
                        zhaofangCount++;
                    }
                }).start();
            }
        });

    }

    int zhaofangCount = 0;

    private String getWebContent(String url) {
        String result = "";
        try {
            HttpClient httpclient = new HttpClient();
            PostMethod httppost = new PostMethod(url);
            httpclient.setTimeout(5000);
            int response = httpclient.executeMethod(httppost);
            if (response >= 200) {
                result = httppost.getResponseBodyAsString();
//              Log.d("TAG", "OK : " + result);
            } else {
                Log.d("TAG", "CUOWU : " + result);
            }
        } catch (Exception e) {
            return "Fail to establish http connection!" + e.toString();
        }
        return result;
    }

    public static int getAvailableSimCardCount(Context context) {
        int count = 0;
        if (isMultiSim(context)) {
            //版本在21及以上
            SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
            for (int i = 0; i < getSimCardCount(context); i++) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return -1;
                }
                SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
                if (sir != null) {
                    count |= 1 << i;
                }
            }
        }
        Log.d("TAG", "当前的电话卡 : " + count);
        return count; //0--无有效卡，1--卡1有效，2---卡2有效，3----卡1，卡2都有效
    }

    public static int getSimCardCount(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);//得到电话管理器实例
        Class cls = mTelephonyManager.getClass();    //得到Class
        try {
            Method mMethod = cls.getMethod("getSimCount");//getSimCount方法反射
            mMethod.setAccessible(true);
            return (int) mMethod.invoke(mTelephonyManager);//反射的方法调用
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isMultiSim(Context context) {
        boolean result = false;
        //版本在21及以上

        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
            result = phoneAccountHandleList.size() >= 2;
        }
        return result;    //true--多卡，false---单卡
    }

    public String formatTime() {
        Date date = new Date();
        String str = "yyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(str);
        return sdf.format(date);
    }

    @SuppressLint("SetTextI18n")
    private void startWork(String phoneNum, int waitSecond) {
        try {
            Log.d("TAG", "当前的电话是: " + phoneNum + ",目标数量是:" + callCount + ",当前已拨打:" + curCount + ",间隔:" + callSpace);

            et_phoneStartNum.setText(phoneNum);
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivity(intent);

            if (textViewLog.getLineCount() > 10 || textViewLog.getText().toString().contains("Log")) {
                textViewLog.setText("");
            }
            textViewLog.setText(textViewLog.getText() + formatTime() + "当前第: " + curCount + "," + phoneNum + "\n");
            handler.sendEmptyMessageDelayed(1, waitSecond * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
