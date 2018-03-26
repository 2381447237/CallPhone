package com.youli.calltest2018_3_22;


import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.lang.reflect.Method;
import com.android.internal.telephony.ITelephony;

public class MainActivity extends CheckPermissionsActivity implements View.OnClickListener {

    private Button btnCall,btnStopCall;//呼叫按钮
    private EditText etNum;
    private String strNum;//输入的电话号码
    private TelephonyManager tm;
    private  Handler handler;
    private Runnable rStartCall;//打电话的任务
    private Runnable rEndCall;//挂断电话的任务

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler=new Handler();

        tm= (TelephonyManager) MainActivity.this.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        etNum = findViewById(R.id.et_phone_num);

        btnCall = findViewById(R.id.btn_call);
        btnCall.setOnClickListener(this);

        btnStopCall=findViewById(R.id.btn_stop_call);
        btnStopCall.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_call://呼 叫

                handler.removeCallbacks(rStartCall);//取消打电话的任务

                callPhone();

                break;

            case R.id.btn_stop_call://停 止 呼 叫

                handler.removeCallbacks(rStartCall);//取消打电话的任务
                handler.removeCallbacks(rEndCall);//取消挂掉电话的任务

                break;
        }
    }

    PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            Log.e("2018-3-23", "state===" + state);
            Log.e("2018-3-23", "tm.getCallState()===" + tm.getCallState());

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:

                    Log.e("2018-3-23", "挂断");

                    rStartCall=new Runnable() {
                        @Override
                        public void run() {
                            //打电话
                            callPhone();
                        }
                    };

                    handler.postDelayed(rStartCall, 2000);//延时2秒
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:

                    Log.e("2018-3-23", "接听");

                    rEndCall=new Runnable() {
                        @Override
                        public void run() {
                          //  挂断电话
                            endcall();
                        }
                    };

                    handler.postDelayed(rEndCall,15000);//延时15秒

                    break;
                case TelephonyManager.CALL_STATE_RINGING:

                   Log.e("2018-3-23", "响铃:来电号码");
                    //输出来电号码
                    break;


            }
       }
    };

    //打电话
    private void callPhone(){

            strNum = etNum.getText().toString().trim();
                if (TextUtils.equals("", strNum)) {
                    Toast.makeText(MainActivity.this, "请输入电话号码!", Toast.LENGTH_SHORT).show();
                } else {
                    handler.removeCallbacks(rEndCall);//取消挂断电话的任务
                    Toast.makeText(MainActivity.this, "电话号码是" + strNum + ",开始打电话", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strNum));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(intent);
                }
    }

    /**
     * 通过反射的方式挂断电话
     */
    public void endcall() {

        Log.e("2018-3-23", "挂电话");

        try {
            //获取到ServiceManager
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            //获取到ServiceManager里面的方法
            Method method = clazz.getDeclaredMethod("getService", String.class);
            //通过反射的方法调用方法

            IBinder iBinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);

            //注意：ITelephony的包名必须是com.android.internal.telephony，不能随便改的

            ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);

            iTelephony.endCall();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
