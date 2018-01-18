package com.mega.sos;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnSafety;
    Button btnLocation;
    Button btnEmergency;
    Button btnSiren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//                Settings.Global.putInt(this.getContentResolver(),
//                        SosUtil.COM_MEGA_SOS_FIRST_START, 1);
        btnSafety = (Button) findViewById(R.id.safety);
        btnEmergency = (Button)findViewById(R.id.emergency);
        btnLocation = (Button)findViewById(R.id.location);
        btnSiren = (Button)findViewById(R.id.siren);
		checkReadPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //布局实现
        getMenuInflater().inflate(R.menu.setings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.settings_item){
            Intent intent=new Intent(MainActivity.this ,SettingsActivity.class);
            startActivity(intent);
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSafetyClick(View view){
		 Intent intent=new Intent(MainActivity.this ,SafetyHomecomingActivity.class);
         startActivity(intent);
    }

    public void onLocationClick(View view){
        Intent intent =new Intent(MainActivity.this,SafetyHomecomingActivity.class);
        //用Bundle携带数据
        Bundle bundle=new Bundle();
        bundle.putString("name", "Location");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onEmergencyClick(View view){
        Intent intent =new Intent(MainActivity.this,SafetyHomecomingActivity.class);
        //用Bundle携带数据
        Bundle bundle=new Bundle();
        bundle.putString("name", "emergency");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onSirenClick(View view){
		 Intent intent=new Intent(MainActivity.this ,SirenActivity.class);
		 startActivity(intent);
    }
	
	 @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_1),
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            //            finish();
            //            System.exit(0);
            moveTaskToBack(true);
        }
    }
	
	// 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };
	
	    private void checkReadPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkReadContactsPermission = checkSelfPermission( android.Manifest.permission.ACCESS_FINE_LOCATION);
            Log.i("MainActivity","checkperdmissons ");
            if (checkReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i("    ","requestPermissions");
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.CALL_PHONE}, 1);
            }
        }
    }

}
