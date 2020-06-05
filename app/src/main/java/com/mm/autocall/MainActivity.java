package com.mm.autocall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    TextView phoneStartNum;
    Button btn_phone2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneStartNum = (TextView) findViewById(R.id.editText);
        btn_phone2 = (Button) findViewById(R.id.btn_phone2);

        /**
         * 在activity 或者 service中加入如下代码，以实现来电状态监听
         */
        TelephonyManager telMgr = (TelephonyManager) this.getSystemService(
                Context.TELEPHONY_SERVICE);
        telMgr.listen(new TelListener(), PhoneStateListener.LISTEN_CALL_STATE);


        final Context context = this;


        btn_phone2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // 开始直接拨打电话
                    Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneStartNum.getText().toString()));
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(intent2);
                    Log.d("TAG","拨打电话 时间 = " + System.currentTimeMillis());
                    Toast.makeText(MainActivity.this, "拨打电话 时间 = " + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            try {
                                // 延迟5秒后自动挂断电话
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
                                Log.d("TAG","挂断电话！时间 = " + System.currentTimeMillis());
                                Toast.makeText(MainActivity.this, "挂断电话！时间 = " + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 15 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
