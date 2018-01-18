package com.mega.sos.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.mega.sos.R;
import com.mega.sos.SafetyHomecomingActivity;
import com.mega.sos.SosUtil;
import com.mega.sos.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author liuhao
 * @time 2017/12/8 18:26
 * @des ${TODO}
 * @email liuhao_nevermore@163.com
 */

public class SafetyHomecomingService extends Service {
    private static final String TAG = "MyService";
    private static String mPhoneNo = "10086";
    private static int timeInterval = 3 * 60;

    private String data = "messageing...";
    private final int PID = android.os.Process.myPid();
    //    private Timer mTimer;
    private String time = "0";
    private int times = 1;
    private CountDownTimer timer;
    private LocationManager mLocationManager;
    private String provider;
    private String mLongitude = "EC";
    Handler mtHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                //                pManager.reboot(null);
            } else if (msg.what == 101) {
                timer = new CountDownTimer(msg.arg1, 1 * 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        time = (millisUntilFinished / 1000) + " s";
                        setForeground();
                    }

                    @Override
                    public void onFinish() {
                        times++;
                        if(mActivity!=null){
                            if(mActivity.getLocationLatitude()!=0 && mActivity.getLocationLongitude()!=0){
                                mLongitude = mActivity.getLocationLatitude()+","+mActivity.getLocationLongitude();
                            }else{
                                getlocationGps();
                            }
                        }else{
                            getlocationGps();
                        }
                        sendSMSMessage();
                        setForeground();
                        sendMessageO();
                    }
                };
                timer.start();
            }
        }
    };
    private SafetyHomecomingActivity mActivity;

    public class Binder extends android.os.Binder{
        public void setData(String data){
            SafetyHomecomingService.this.data = data;
        }
        public SafetyHomecomingService getMyService(){
            return SafetyHomecomingService.this;
        }
    }
    public void setFlashData(){
        if(mActivity!=null){
            if(mActivity.getLocationLatitude()!=0 && mActivity.getLocationLongitude()!=0){
                mLongitude = mActivity.getLocationLatitude()+","+mActivity.getLocationLongitude();
            }else{
                getlocationGps();
            }
        }else{
            getlocationGps();
        }
        sendSMSMessage();
    }
    private Callback callback;
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }
    public static interface Callback{
        void onDataChange(String data);
    }


    protected void sendSMSMessage() {
        String message = "SOS !\n" + getResources().getString(R.string.sos_message_left) + mLongitude +
                getResources().getString(R.string.sos_message_right);
        Boolean[] isSendSuccess = new Boolean[3];
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String mPhoneNo1 = Utils.getStringShare(this, SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER1);
            String mPhoneNo2 = Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER2);
            String mPhoneNo3 = Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER3);

            String CurrentPhoneName = "";
            if (!"".equals(mPhoneNo1)){
                Log.d(TAG,"liuhao mPhoneNo1:"+mPhoneNo1+" message:"+message);
                smsManager.sendTextMessage(mPhoneNo1, null, message, null, null);
                isSendSuccess[0] = true;
                CurrentPhoneName += Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NAME1)+",";
            }else{
                isSendSuccess[0] = false;
            }
            if (!"".equals(mPhoneNo2)){
                smsManager.sendTextMessage(mPhoneNo2, null, message, null, null);
                isSendSuccess[1] = true;
                CurrentPhoneName += Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NAME2)+",";
            }else{
                isSendSuccess[1] = false;
            }
            if (!"".equals(mPhoneNo3)){
                smsManager.sendTextMessage(mPhoneNo3, null, message, null, null);
                isSendSuccess[2] = true;
                CurrentPhoneName += Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NAME3);
            }else{
                isSendSuccess[2] = false;
            }
            if(CurrentPhoneName == ""){
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_4),
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), CurrentPhoneName+getResources().getString(R.string.toast_2),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.e(TAG, "onStart()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        timer.cancel();
    }

    public void sendMessageO() {
        Message obtain = Message.obtain();
        obtain.what = 101;
        int timeInterval = Utils.getSendSMSTimeInterval(this);
        obtain.arg1 = timeInterval;
        mtHandler.sendMessage(obtain);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivity = new SafetyHomecomingActivity();
        if(mActivity!=null){
            if(mActivity.getLocationLatitude()!=0 && mActivity.getLocationLongitude()!=0){
                mLongitude = mActivity.getLocationLatitude()+","+mActivity.getLocationLongitude();
            }else{
                getlocationGps();
            }
        }else{
            getlocationGps();
        }
        sendMessageO();
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentInfo(getResources().getString(R.string.send) + ":" + times);
        builder.setContentText(getResources().getString(R.string.count_down) + time.toString());
        builder.setContentTitle(getResources().getString(R.string.shared_location));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("new message");
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        Intent intent = new Intent(this, SafetyHomecomingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }

    public void setForeground() {
        this.startForeground(PID, getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private void getlocationGps() {
        //此处的判定是主要问题，API23之后需要先判断之后才能调用locationManager中的方法
        //包括这里的getLastKnewnLocation方法和requestLocationUpdates方法
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //获取定位服务
            if (mLocationManager == null)
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //获取当前可用的位置控制器
            List<String> list = mLocationManager.getProviders(true);
            for (String a : list) {
                Log.d(TAG, "liuhao:" + a);
            }
            if (list.contains(LocationManager.GPS_PROVIDER)) {
                //是否为GPS位置控制器
                provider = LocationManager.GPS_PROVIDER;
            } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                //是否为网络位置控制器
                provider = LocationManager.NETWORK_PROVIDER;

            } else {
                Toast.makeText(this, "Please check whether the network or GPS is open",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (location != null) {
                //获取当前位置，这里只用到了经纬度
                mLongitude = location.getLatitude() + "," + location.getLongitude();
                if(callback!=null)
                callback.onDataChange(mLongitude);
            }
            //绑定定位事件，监听位置是否改变
            //第一个参数为控制器类型第二个参数为监听位置变化的时间间隔（单位：毫秒）
            //第三个参数为位置变化的间隔（单位：米）第四个参数为位置监听器
            mLocationManager.requestLocationUpdates(provider, 5000, 2, locationListener);
        }
    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "liu onLocationChanged");
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "liu onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "liu onProviderEnabled");

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "liu onProviderDisabled");
        }
    };

    public void showLocation(Location currentLocation) {
        if (currentLocation != null) {
            String s = "";
            s += currentLocation.getLongitude();
            s += ",";
            s += currentLocation.getLatitude();
            mLongitude = s;
            if(callback!=null)
            callback.onDataChange(mLongitude);
        } else {

        }
    }
}
