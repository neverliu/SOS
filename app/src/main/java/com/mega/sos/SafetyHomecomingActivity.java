package com.mega.sos;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.mega.sos.service.SafetyHomecomingService;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuhao
 * @time 2017/12/8 14:59
 * @des ${TODO}
 * @email liuhao_nevermore@163.com
 */

public class SafetyHomecomingActivity extends AppCompatActivity implements ServiceConnection {

    private TextView mNavigationtile;
    private TextView mLocation;
    private TextView mEmergencyText;
    private Button safetyhomeBtn;
    private static final String TAG = "SafetyActivity";
    private SafetyHomecomingService myService;
    public boolean CURRENTSWITCH = false;
    public boolean isFirstLoc = true;
    private Intent serviceIntent;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mLocation.setText(msg.obj.toString());
        }
    };
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private float currentZoom = 18;
    private BitmapDescriptor mCurrentMarker;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private double mLatitude;
    private double mLongitude;
    private boolean isLocationActivity = false;
    private boolean isEmergencyActivity = false;
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "liuhao onServiceConnected");
        SafetyHomecomingService.Binder binder = (SafetyHomecomingService.Binder) service;
        SafetyHomecomingService myService = binder.getMyService();
        myService.setCallback(new SafetyHomecomingService.Callback() {
            @Override
            public void onDataChange(String data) {
                Log.d(TAG, "liuhao onDataChange");
                Message msg = new Message();
                msg.obj = data;
                handler.sendMessage(msg);
            }
        });
        //第一次发送短信
        myService.setFlashData();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(isLocationActivity){
                finish();
            }else if(isEmergencyActivity){
                finish();
            }else{
                if (CURRENTSWITCH) {
                    //                startActivity(new Intent(this, MainActivity.class));

                    //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                    AlertDialog.Builder builder = new AlertDialog.Builder(SafetyHomecomingActivity.this);
                    //                //    设置Title的内容
                    //                builder.setTitle("弹出警告框");
                    //    设置Content来显示一个信息
                    builder.setMessage(getResources().getString(R.string.Dialog_1));
                    //    设置一个PositiveButton
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            stopService(serviceIntent);
                            unbindService(SafetyHomecomingActivity.this);
                            finish();
                        }
                    });
                    //    设置一个NegativeButton
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder.show();
                    return false;
                } else {
                    finish();
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    }
    private void delectBaiduIcon(){
        // 隐藏logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }

        //地图上比例尺
        mMapView.showScaleControl(false);


        // 隐藏缩放控件
        mMapView.showZoomControls(false);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        //获取地图控件引用

        setContentView(R.layout.activity_safetyhome);
        //新页面接收数据
        Bundle bundle = this.getIntent().getExtras();
        //接收name值
        if(bundle!=null){
            String name = bundle.getString("name");
            if(!name.equals("") || name!=null){
                if(name.equals("Location")){
                    isLocationActivity = true;
                }else if(name.equals("emergency")){
                    isEmergencyActivity = true;
                }
            }
        }
        mMapView = (MapView) findViewById(R.id.bmapView);
        delectBaiduIcon();
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        mBaiduMap = mMapView.getMap();
//        设置中心点为韩国首尔
        setMapCenter();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        initLocation();
//        LocationClientOption option = new LocationClientOption();
//        option.setOpenGps(true);// 打开gps
//        option.setCoorType("bd09ll"); // 设置坐标类型
//        option.setScanSpan(5000);
//        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
         mCurrentMarker =null ; // 默认图标
//        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));


        mNavigationtile = (TextView) findViewById(R.id.navigation_tile);
        mLocation = (TextView) findViewById(R.id.location);
        mEmergencyText = (TextView) findViewById(R.id.emergency_text);
        safetyhomeBtn = (Button) findViewById(R.id.safetyhome_btn);
        if(isLocationActivity){
            safetyhomeBtn.setText(getResources().getString(R.string.send_current_location));
        }else if(isEmergencyActivity){
            mEmergencyText.setVisibility(View.VISIBLE);
            safetyhomeBtn.setText(getResources().getString(R.string.emergency_sos));
            safetyhomeBtn.setBackgroundResource(R.color.colorRed);
        }
        serviceIntent = new Intent(SafetyHomecomingActivity.this, SafetyHomecomingService.class);
        safetyhomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLocationActivity){
                    if (CURRENTSWITCH) {
                        //do nothing
                    }else{
                        safetyhomeBtn.setBackgroundResource(R.color.colorLightGray);
                        safetyhomeBtn.setEnabled(false);
                        sendSMSMessage(mLatitude + "," + mLongitude);
                        CURRENTSWITCH = true;
                    }
                }else if(isEmergencyActivity){
                    if (CURRENTSWITCH) {
                        //do nothing
                    }else{
//                        safetyhomeBtn.setVisibility(View.GONE);
                        finish();
                        sendEMERSMSMessage(mLatitude + "," + mLongitude);
                        EmergencyCall();
                        CURRENTSWITCH = true;
                    }
                }else{
                    if (CURRENTSWITCH) {
                        mNavigationtile.setVisibility(View.GONE);
                        safetyhomeBtn.setText(getResources().getString(R.string.send));
                        safetyhomeBtn.setBackgroundResource(R.color.colorGreen);
                        CURRENTSWITCH = false;
                        if (serviceIntent != null)
                            stopService(serviceIntent);
                        unbindService(SafetyHomecomingActivity.this);
                    } else {
                        mNavigationtile.setVisibility(View.VISIBLE);
                        safetyhomeBtn.setText(getResources().getString(R.string.stop));
                        safetyhomeBtn.setBackgroundResource(R.color.colorRed);
                        CURRENTSWITCH = true;
                        if (serviceIntent != null)
                            startService(serviceIntent);
                        bindService(serviceIntent, SafetyHomecomingActivity.this, BIND_AUTO_CREATE);
                    }
                }
            }
        });
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(isLocationActivity){
                actionBar.setTitle(getResources().getString(R.string.send_current_location));
            }else if(isEmergencyActivity){
                actionBar.setTitle(getResources().getString(R.string.emergency_sos));
            }else{
                actionBar.setTitle(getResources().getString(R.string.at_home));
            }
        }
    }
    protected void EmergencyCall(){
        String callNum = Utils.getStringShare(this,SosUtil.SHARESP_CALL_CONTACT_NUMBER);
        Log.d(TAG,"liuhao:"+callNum);
        if(callNum == null || "".equals(callNum)){
            showChangeDialog();
            Toast.makeText(this,getResources().getString(R.string.toast_5),Toast.LENGTH_SHORT).show();
        }else{
            //call
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int checkReadContactsPermission = checkSelfPermission(Manifest.permission.CALL_PHONE);
                Log.i("MainActivity", "checkperdmissons ");
                if (checkReadContactsPermission == PackageManager.PERMISSION_GRANTED) {
                    Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callNum));
                    startActivity(dialIntent);
                }else{
                    requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                }
            }else{
                Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callNum));
                startActivity(dialIntent);
            }
        }
    }

    private void showChangeDialog(){
        Intent intent = new Intent(this, EMERContactSettingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startActivityForResult(int code){
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI), code);
    }

    protected void sendEMERSMSMessage(String address) {
        String message = getResources().getString(R.string.sos_message_2_left) + address +
                getResources().getString(R.string.sos_message_2_right);
        Boolean[] isSendSuccess = new Boolean[3];
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String mPhoneNo1 = Utils.getStringShare(this,SosUtil.SHARESP_EMER_SMS_CONTACT_NUMBER1);
            String mPhoneNo2 = Utils.getStringShare(this,SosUtil.SHARESP_EMER_SMS_CONTACT_NUMBER2);
            String mPhoneNo3 = Utils.getStringShare(this,SosUtil.SHARESP_EMER_SMS_CONTACT_NUMBER3);

            String CurrentPhoneName = "";
            if (!"".equals(mPhoneNo1)){
                smsManager.sendTextMessage(mPhoneNo1, null, message, null, null);
                isSendSuccess[0] = true;
                CurrentPhoneName += Utils.getStringShare(this,SosUtil.SHARESP_EMER_SMS_CONTACT_NAME1)+",";
            }else{
                isSendSuccess[0] = false;
            }
            if (!"".equals(mPhoneNo2)){
                smsManager.sendTextMessage(mPhoneNo2, null, message, null, null);
                isSendSuccess[1] = true;
                CurrentPhoneName += Utils.getStringShare(this,SosUtil.SHARESP_EMER_SMS_CONTACT_NAME2)+",";
            }else{
                isSendSuccess[1] = false;
            }
            if (!"".equals(mPhoneNo3)){
                smsManager.sendTextMessage(mPhoneNo3, null, message, null, null);
                isSendSuccess[2] = true;
                CurrentPhoneName += Utils.getStringShare(this,SosUtil.SHARESP_EMER_SMS_CONTACT_NAME3);
            }else{
                isSendSuccess[2] = false;
            }
            if(CurrentPhoneName == ""){
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_3),
                        Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(), CurrentPhoneName+getResources().getString(R.string.toast_2),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    protected void sendSMSMessage(String address) {
        String message = getResources().getString(R.string.sos_message_1) + address;

        Boolean[] isSendSuccess = new Boolean[3];
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String mPhoneNo1 = Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER1);
            String mPhoneNo2 = Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER2);
            String mPhoneNo3 = Utils.getStringShare(this,SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER3);

            String CurrentPhoneName = "";
            if (!"".equals(mPhoneNo1)){
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(isLocationActivity){
                    finish();
                    return false;
                }else if(isEmergencyActivity){
                    finish();
                    return false;
                }else{
                    if (CURRENTSWITCH) {
                        //                    startActivity(new Intent(this, MainActivity.class));
                        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                        AlertDialog.Builder builder = new AlertDialog.Builder(SafetyHomecomingActivity.this);
                        //                //    设置Title的内容
                        //                builder.setTitle("弹出警告框");
                        //    设置Content来显示一个信息
                        builder.setMessage(getResources().getString(R.string.Dialog_1));
                        //    设置一个PositiveButton
                        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                stopService(serviceIntent);
                                unbindService(SafetyHomecomingActivity.this);
                                finish();
                            }
                        });
                        //    设置一个NegativeButton
                        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });
                        builder.show();
                        return true;
                    } else {
                        finish();
                        return false;
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMapCenter(){
        //设定中心点坐标
        LatLng cenpt =  new LatLng(37.33,126.58);
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(cenpt)
                .zoom(18.0f)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=2000;//每隔2秒钟计算位置
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    public double getLocationLatitude(){
        return mLatitude;
    }
    public double getLocationLongitude(){
        return mLongitude;
    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();
            Log.d(TAG,"liuhao mLatitude:"+mLatitude+" mLongitude:"+mLongitude);
            if(bdLocation == null){
                Toast.makeText(SafetyHomecomingActivity.this,"location is null",Toast.LENGTH_SHORT).show();
                return;
            }
            MyLocationData locData = new MyLocationData.Builder().accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            //地图SDK处理
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            LatLng point = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            OverlayOptions dotOption = new DotOptions().center(point).color(0xAAFF0000);
            mBaiduMap.addOverlay(dotOption);
        }

        private void logString(BDLocation location){
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

}
